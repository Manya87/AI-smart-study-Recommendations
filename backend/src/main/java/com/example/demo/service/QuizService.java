package com.example.demo.service;

import com.example.demo.dto.QuizCreateRequest;
import com.example.demo.dto.QuizResponse;
import com.example.demo.dto.QuizResultResponse;
import com.example.demo.dto.QuizSubmitRequest;

public interface QuizService {
    QuizResponse createQuiz(QuizCreateRequest request);
    QuizResultResponse submitQuiz(QuizSubmitRequest request);
}
