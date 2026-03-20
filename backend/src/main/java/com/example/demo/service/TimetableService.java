package com.example.demo.service;

import com.example.demo.dto.TimetableResponse;

public interface TimetableService {
    TimetableResponse generateTimetable(Long studentId, Integer availableHours);
}
