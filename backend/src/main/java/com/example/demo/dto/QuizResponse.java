package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private Integer totalQuestions;
    private Long topicId;
    private String topicName;
}
