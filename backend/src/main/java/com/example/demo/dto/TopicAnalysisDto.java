package com.example.demo.dto;

import com.example.demo.entity.WeaknessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopicAnalysisDto {
    private Long topicId;
    private String topicName;
    private Double averageScore;
    private Double confidenceScore;
    private Integer totalAttempts;
    private Integer averageMistakes;
    private WeaknessLevel weaknessLevel;
    private Integer weaknessRank;
}
