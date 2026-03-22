package com.example.demo.service.impl;

import com.example.demo.dto.TimetableEntryResponse;
import com.example.demo.dto.TimetableResponse;
import com.example.demo.dto.TopicAnalysisDto;
import com.example.demo.entity.WeaknessLevel;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.AnalysisService;
import com.example.demo.service.TimetableService;
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
public class TimetableServiceImpl implements TimetableService {

    private final StudentRepository studentRepository;
    private final AnalysisService analysisService;

    @Override
    @Transactional(readOnly = true)
    public TimetableResponse generateTimetable(Long studentId, Integer availableHours) {
        Long safeStudentId = Objects.requireNonNull(studentId, "studentId must not be null");
        int dailyHours = availableHours != null ? availableHours : studentRepository.findById(safeStudentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId))
            .getDailyStudyHours();

        if (dailyHours <= 0) {
            throw new IllegalArgumentException("Available hours must be greater than zero");
        }

        List<TopicAnalysisDto> rankedTopics = new ArrayList<>(analysisService.getWeakTopicRanking(safeStudentId));
        if (rankedTopics.isEmpty()) {
            return TimetableResponse.builder()
                .studentId(studentId)
                .totalAvailableHours(dailyHours)
                .schedule(List.of())
                .build();
        }

        rankedTopics.sort(Comparator
            .comparingInt((TopicAnalysisDto dto) -> weaknessWeight(dto.getWeaknessLevel())).reversed()
            .thenComparing(TopicAnalysisDto::getWeaknessRank));

        Map<String, Integer> allocation = new HashMap<>();
        int remaining = dailyHours;
        int index = 0;

        // Greedy loop: repeatedly allocate one hour to the most weak topics first.
        while (remaining > 0) {
            TopicAnalysisDto topic = rankedTopics.get(index % rankedTopics.size());
            String topicName = topic.getTopicName();
            allocation.put(topicName, allocation.getOrDefault(topicName, 0) + 1);
            remaining--;
            index++;
        }

        List<TimetableEntryResponse> entries = rankedTopics.stream()
            .filter(topic -> allocation.containsKey(topic.getTopicName()))
            .map(topic -> TimetableEntryResponse.builder()
                .topicName(topic.getTopicName())
                .allocatedHours(allocation.get(topic.getTopicName()))
                .reason("Priority " + topic.getWeaknessRank() + " - " + topic.getWeaknessLevel())
                .build())
            .toList();

        log.info("Generated timetable for student={} with {} available hours", studentId, dailyHours);
        return TimetableResponse.builder()
            .studentId(studentId)
            .totalAvailableHours(dailyHours)
            .schedule(entries)
            .build();
    }

    private int weaknessWeight(WeaknessLevel level) {
        return switch (level) {
            case CRITICAL -> 3;
            case WEAK -> 2;
            case NORMAL -> 1;
        };
    }
}
