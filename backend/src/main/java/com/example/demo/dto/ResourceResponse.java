package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResourceResponse {
    private Long id;
    private String title;
    private String url;
    private String description;
}
