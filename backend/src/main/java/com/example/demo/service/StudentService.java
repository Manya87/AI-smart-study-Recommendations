package com.example.demo.service;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;

public interface StudentService {
    StudentResponse createStudent(StudentRequest request);
    StudentResponse updateStudent(Long studentId, StudentRequest request);
    StudentResponse getStudent(Long studentId);
}
