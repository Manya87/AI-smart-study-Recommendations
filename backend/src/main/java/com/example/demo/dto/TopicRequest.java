package com.example.demo.dto;

import com.example.demo.entity.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicRequest {

    @NotBlank
    private String name;

    @NotNull
    private DifficultyLevel difficultyLevel;
}
