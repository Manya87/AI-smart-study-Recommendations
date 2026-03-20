package com.example.demo.controller;

import com.example.demo.dto.QuizCreateRequest;
import com.example.demo.dto.QuizResponse;
import com.example.demo.dto.QuizResultResponse;
import com.example.demo.dto.QuizSubmitRequest;
import com.example.demo.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> createQuiz(@Valid @RequestBody QuizCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuiz(request));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<QuizResultResponse> submitQuiz(@Valid @RequestBody QuizSubmitRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(request));
    }
}
