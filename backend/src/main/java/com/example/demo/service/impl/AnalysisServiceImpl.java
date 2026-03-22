package com.example.demo.service.impl;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.dto.TopicAnalysisDto;
import com.example.demo.entity.QuizResult;
import com.example.demo.entity.WeaknessLevel;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.QuizResultRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.AnalysisService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements AnalysisService {

    private final StudentRepository studentRepository;
    private final QuizResultRepository quizResultRepository;

    @Override
    @Transactional(readOnly = true)
    public AnalysisResponse analyzeStudent(Long studentId) {
        Long safeStudentId = Objects.requireNonNull(studentId, "studentId must not be null");
        studentRepository.findById(safeStudentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        List<QuizResult> results = quizResultRepository.findByStudentId(safeStudentId);
        List<TopicAnalysisDto> ranking = buildTopicAnalysis(results);

        return AnalysisResponse.builder()
            .studentId(studentId)
            .totalQuizzesTaken(results.size())
            .topics(ranking)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicAnalysisDto> getWeakTopicRanking(Long studentId) {
        Long safeStudentId = Objects.requireNonNull(studentId, "studentId must not be null");
        studentRepository.findById(safeStudentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        List<QuizResult> results = quizResultRepository.findByStudentId(safeStudentId);
        return buildTopicAnalysis(results);
    }

    private List<TopicAnalysisDto> buildTopicAnalysis(List<QuizResult> results) {
        if (results.isEmpty()) {
            return List.of();
        }

        Map<Long, List<QuizResult>> byTopic = new HashMap<>();
        for (QuizResult result : results) {
            byTopic.computeIfAbsent(result.getTopic().getId(), key -> new ArrayList<>()).add(result);
        }

        List<TopicAnalysisDto> analysis = new ArrayList<>();
        for (Map.Entry<Long, List<QuizResult>> entry : byTopic.entrySet()) {
            List<QuizResult> topicResults = entry.getValue();
            String topicName = topicResults.get(0).getTopic().getName();

            double avgScore = topicResults.stream().mapToDouble(QuizResult::getScore).average().orElse(0.0);
            int avgMistakes = (int) Math.round(topicResults.stream().mapToInt(QuizResult::getMistakesCount).average().orElse(0.0));
            WeaknessLevel weaknessLevel = evaluateWeakness(avgScore, avgMistakes);
            double confidence = calculateConfidence(topicResults, avgScore, avgMistakes);

            analysis.add(TopicAnalysisDto.builder()
                .topicId(entry.getKey())
                .topicName(topicName)
                .averageScore(round(avgScore))
                .averageMistakes(avgMistakes)
                .totalAttempts(topicResults.size())
                .weaknessLevel(weaknessLevel)
                .confidenceScore(round(confidence))
                .build());
        }

        analysis.sort(Comparator
            .comparingInt((TopicAnalysisDto dto) -> weaknessWeight(dto.getWeaknessLevel())).reversed()
            .thenComparing(TopicAnalysisDto::getAverageScore)
            .thenComparing(TopicAnalysisDto::getAverageMistakes, Comparator.reverseOrder()));

        List<TopicAnalysisDto> ranked = new ArrayList<>();
        for (int i = 0; i < analysis.size(); i++) {
            TopicAnalysisDto dto = analysis.get(i);
            ranked.add(TopicAnalysisDto.builder()
                .topicId(dto.getTopicId())
                .topicName(dto.getTopicName())
                .averageScore(dto.getAverageScore())
                .averageMistakes(dto.getAverageMistakes())
                .totalAttempts(dto.getTotalAttempts())
                .weaknessLevel(dto.getWeaknessLevel())
                .confidenceScore(dto.getConfidenceScore())
                .weaknessRank(i + 1)
                .build());
        }

        log.debug("Built topic analysis for {} topics", ranked.size());
        return ranked;
    }

    private WeaknessLevel evaluateWeakness(double avgScore, int avgMistakes) {
        if (avgMistakes > 3) {
            return WeaknessLevel.CRITICAL;
        }
        if (avgScore < 50.0) {
            return WeaknessLevel.WEAK;
        }
        return WeaknessLevel.NORMAL;
    }

    private double calculateConfidence(List<QuizResult> topicResults, double avgScore, int avgMistakes) {
        double trend = 0.0;
        if (topicResults.size() >= 2) {
            double first = topicResults.get(0).getScore();
            double last = topicResults.get(topicResults.size() - 1).getScore();
            trend = (last - first) * 0.4;
        }

        double confidence = avgScore - (avgMistakes * 6.0) + trend;
        if (confidence < 0) {
            return 0;
        }
        if (confidence > 100) {
            return 100;
        }
        return confidence;
    }

    private int weaknessWeight(WeaknessLevel level) {
        return switch (level) {
            case CRITICAL -> 3;
            case WEAK -> 2;
            case NORMAL -> 1;
        };
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
