package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimetableEntryResponse {
    private String topicName;
    private int allocatedHours;
    private String reason;
}
