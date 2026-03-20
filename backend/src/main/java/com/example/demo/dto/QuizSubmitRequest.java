package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSubmitRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long quizId;

    @NotNull
    @Min(0)
    @Max(100)
    private Double score;

    @NotNull
    @Min(1)
    private Integer timeTaken;

    @NotNull
    @Min(0)
    private Integer mistakesCount;
}
