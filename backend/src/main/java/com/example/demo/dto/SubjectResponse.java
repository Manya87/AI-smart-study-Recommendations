package com.example.demo.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubjectResponse {
    private Long id;
    private String name;
    private List<TopicResponse> topics;
}
