package com.example.demo.controller;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<AnalysisResponse> getAnalysis(@PathVariable Long studentId) {
        return ResponseEntity.ok(analysisService.analyzeStudent(studentId));
    }
}
