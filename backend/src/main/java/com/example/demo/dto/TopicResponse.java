package com.example.demo.dto;

import com.example.demo.entity.DifficultyLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopicResponse {
    private Long id;
    private String name;
    private DifficultyLevel difficultyLevel;
    private Long subjectId;
    private String subjectName;
}
