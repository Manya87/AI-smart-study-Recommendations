package com.example.demo.service;

import com.example.demo.dto.ResourceRequest;
import com.example.demo.dto.ResourceResponse;
import com.example.demo.dto.SubjectRequest;
import com.example.demo.dto.SubjectResponse;
import com.example.demo.dto.TopicRequest;
import com.example.demo.dto.TopicResponse;

public interface SubjectService {
    SubjectResponse createSubject(SubjectRequest request);
    SubjectResponse getSubject(Long subjectId);
    TopicResponse addTopic(Long subjectId, TopicRequest request);
    ResourceResponse addResource(Long topicId, ResourceRequest request);
}
