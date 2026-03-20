package com.example.demo.service.impl;

import com.example.demo.dto.RecommendationItemResponse;
import com.example.demo.dto.RecommendationResponse;
import com.example.demo.dto.ResourceResponse;
import com.example.demo.dto.TopicAnalysisDto;
import com.example.demo.entity.Recommendation;
import com.example.demo.entity.ResourceLink;
import com.example.demo.entity.Student;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.RecommendationRepository;
import com.example.demo.repository.ResourceLinkRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TopicRepository;
import com.example.demo.service.AnalysisService;
import com.example.demo.service.RecommendationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final StudentRepository studentRepository;
    private final AnalysisService analysisService;
    private final ResourceLinkRepository resourceLinkRepository;
    private final RecommendationRepository recommendationRepository;
    private final TopicRepository topicRepository;

    @Override
    @Transactional
    @Cacheable(cacheNames = "recommendations", key = "#studentId")
    public RecommendationResponse getRecommendations(Long studentId) {
        Long safeStudentId = Objects.requireNonNull(studentId, "studentId must not be null");
        Student student = studentRepository.findById(safeStudentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        List<TopicAnalysisDto> rankedTopics = analysisService.getWeakTopicRanking(safeStudentId);
        recommendationRepository.deleteByStudentId(safeStudentId);

        List<RecommendationItemResponse> items = rankedTopics.stream()
            .map(topic -> buildRecommendationItem(student, topic))
            .toList();

        log.info("Generated {} recommendations for student={}", items.size(), studentId);
        return RecommendationResponse.builder()
            .studentId(studentId)
            .recommendations(items)
            .build();
    }

    private RecommendationItemResponse buildRecommendationItem(Student student, TopicAnalysisDto topic) {
        List<ResourceLink> resources = resourceLinkRepository.findByTopicId(topic.getTopicId());
        List<ResourceResponse> resourceResponses = resources.stream()
            .map(resource -> ResourceResponse.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .url(resource.getUrl())
                .description(resource.getDescription())
                .build())
            .toList();

        String recommendationText = "Focus on " + topic.getTopicName() + " due to "
            + topic.getWeaknessLevel().name().toLowerCase() + " performance."
            + " Revise theory and solve 2 practice quizzes.";

        Recommendation recommendation = new Recommendation();
        recommendation.setStudent(student);
        Long safeTopicId = Objects.requireNonNull(topic.getTopicId(), "topicId must not be null");
        recommendation.setTopic(topicRepository.findById(safeTopicId)
            .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topic.getTopicId())));
        recommendation.setResource(resources.isEmpty() ? null : resources.get(0));
        recommendation.setWeaknessLevel(topic.getWeaknessLevel());
        recommendation.setPriority(topic.getWeaknessRank());
        recommendation.setRecommendationText(recommendationText);
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendationRepository.save(recommendation);

        return RecommendationItemResponse.builder()
            .topicName(topic.getTopicName())
            .weaknessLevel(topic.getWeaknessLevel())
            .priority(topic.getWeaknessRank())
            .recommendationText(recommendationText)
            .resources(resourceResponses)
            .build();
    }
}
