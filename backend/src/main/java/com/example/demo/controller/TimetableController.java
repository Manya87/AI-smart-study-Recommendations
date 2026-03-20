package com.example.demo.controller;

import com.example.demo.dto.TimetableResponse;
import com.example.demo.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<TimetableResponse> getTimetable(
        @PathVariable Long studentId,
        @RequestParam(required = false) Integer availableHours
    ) {
        return ResponseEntity.ok(timetableService.generateTimetable(studentId, availableHours));
    }
}
