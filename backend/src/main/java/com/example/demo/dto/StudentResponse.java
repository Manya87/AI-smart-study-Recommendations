package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentResponse {
    private Long id;
    private String name;
    private String email;
    private String targetGoal;
    private Integer dailyStudyHours;
}
