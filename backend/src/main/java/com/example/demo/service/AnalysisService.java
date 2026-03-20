package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.dto.TopicAnalysisDto;
import java.util.List;

public interface AnalysisService {
    AnalysisResponse analyzeStudent(Long studentId);
    List<TopicAnalysisDto> getWeakTopicRanking(Long studentId);
}
