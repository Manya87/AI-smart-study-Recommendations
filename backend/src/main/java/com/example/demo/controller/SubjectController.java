package com.example.demo.controller;

import com.example.demo.dto.ResourceRequest;
import com.example.demo.dto.ResourceResponse;
import com.example.demo.dto.SubjectRequest;
import com.example.demo.dto.SubjectResponse;
import com.example.demo.dto.TopicRequest;
import com.example.demo.dto.TopicResponse;
import com.example.demo.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.createSubject(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable("id") Long subjectId) {
        return ResponseEntity.ok(subjectService.getSubject(subjectId));
    }

    @PostMapping("/{subjectId}/topics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TopicResponse> addTopic(
        @PathVariable Long subjectId,
        @Valid @RequestBody TopicRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.addTopic(subjectId, request));
    }

    @PostMapping("/topics/{topicId}/resources")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResourceResponse> addResource(
        @PathVariable Long topicId,
        @Valid @RequestBody ResourceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.addResource(topicId, request));
    }
}
