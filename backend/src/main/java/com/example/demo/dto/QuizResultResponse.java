package com.example.demo.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResultResponse {
    private Long resultId;
    private Long studentId;
    private Long quizId;
    private Long topicId;
    private String topicName;
    private Double score;
    private Integer timeTaken;
    private Integer mistakesCount;
    private LocalDate date;
}
