package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizCreateRequest {

    @NotBlank
    private String title;

    @NotNull
    private Long topicId;

    @NotNull
    @Min(1)
    private Integer totalQuestions;
}
