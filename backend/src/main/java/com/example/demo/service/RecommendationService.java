package com.example.demo.service;

import com.example.demo.dto.RecommendationResponse;

public interface RecommendationService {
    RecommendationResponse getRecommendations(Long studentId);
}
