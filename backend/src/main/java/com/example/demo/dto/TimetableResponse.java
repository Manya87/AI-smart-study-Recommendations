package com.example.demo.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimetableResponse {
    private Long studentId;
    private int totalAvailableHours;
    private List<TimetableEntryResponse> schedule;
}
