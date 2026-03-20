package com.example.demo.dto;

import com.example.demo.entity.WeaknessLevel;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendationItemResponse {
    private String topicName;
    private WeaknessLevel weaknessLevel;
    private int priority;
    private String recommendationText;
    private List<ResourceResponse> resources;
}
