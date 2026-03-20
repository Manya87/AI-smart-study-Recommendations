package com.example.demo.service.impl;

import com.example.demo.dto.ResourceRequest;
import com.example.demo.dto.ResourceResponse;
import com.example.demo.dto.SubjectRequest;
import com.example.demo.dto.SubjectResponse;
import com.example.demo.dto.TopicRequest;
import com.example.demo.dto.TopicResponse;
import com.example.demo.entity.ResourceLink;
import com.example.demo.entity.Subject;
import com.example.demo.entity.Topic;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ResourceLinkRepository;
import com.example.demo.repository.SubjectRepository;
import com.example.demo.repository.TopicRepository;
import com.example.demo.service.SubjectService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final ResourceLinkRepository resourceLinkRepository;

    @Override
    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        subjectRepository.findByNameIgnoreCase(request.getName()).ifPresent(existing -> {
            throw new DuplicateResourceException("Subject already exists: " + request.getName());
        });

        Subject subject = new Subject();
        subject.setName(request.getName().trim());
        Subject saved = subjectRepository.save(subject);
        log.info("Created subject id={} name={}", saved.getId(), saved.getName());

        return SubjectResponse.builder()
            .id(saved.getId())
            .name(saved.getName())
            .topics(List.of())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubject(Long subjectId) {
        Long safeSubjectId = Objects.requireNonNull(subjectId, "subjectId must not be null");
        Subject subject = subjectRepository.findById(safeSubjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));

        List<TopicResponse> topics = topicRepository.findBySubjectId(safeSubjectId).stream()
            .map(this::toTopicResponse)
            .toList();

        return SubjectResponse.builder()
            .id(subject.getId())
            .name(subject.getName())
            .topics(topics)
            .build();
    }

    @Override
    @Transactional
    public TopicResponse addTopic(Long subjectId, TopicRequest request) {
        Long safeSubjectId = Objects.requireNonNull(subjectId, "subjectId must not be null");
        Subject subject = subjectRepository.findById(safeSubjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));

        Topic topic = new Topic();
        topic.setName(request.getName().trim());
        topic.setDifficultyLevel(request.getDifficultyLevel());
        topic.setSubject(subject);

        Topic saved = topicRepository.save(topic);
        log.info("Added topic id={} to subject={}", saved.getId(), subjectId);
        return toTopicResponse(saved);
    }

    @Override
    @Transactional
    public ResourceResponse addResource(Long topicId, ResourceRequest request) {
        Long safeTopicId = Objects.requireNonNull(topicId, "topicId must not be null");
        Topic topic = topicRepository.findById(safeTopicId)
            .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        ResourceLink resource = new ResourceLink();
        resource.setTitle(request.getTitle().trim());
        resource.setUrl(request.getUrl().trim());
        resource.setDescription(request.getDescription());
        resource.setTopic(topic);

        ResourceLink saved = resourceLinkRepository.save(resource);
        log.info("Added resource id={} for topic={}", saved.getId(), topicId);

        return ResourceResponse.builder()
            .id(saved.getId())
            .title(saved.getTitle())
            .url(saved.getUrl())
            .description(saved.getDescription())
            .build();
    }

    private TopicResponse toTopicResponse(Topic topic) {
        return TopicResponse.builder()
            .id(topic.getId())
            .name(topic.getName())
            .difficultyLevel(topic.getDifficultyLevel())
            .subjectId(topic.getSubject().getId())
            .subjectName(topic.getSubject().getName())
            .build();
    }
}
