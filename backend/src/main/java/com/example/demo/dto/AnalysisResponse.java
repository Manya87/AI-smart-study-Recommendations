package com.example.demo.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisResponse {
    private Long studentId;
    private int totalQuizzesTaken;
    private List<TopicAnalysisDto> topics;
}
