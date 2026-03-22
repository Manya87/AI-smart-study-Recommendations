package com.example.demo.service.impl;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;
import com.example.demo.entity.Student;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.StudentRepository;
import java.util.Objects;
import com.example.demo.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        studentRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new DuplicateResourceException("Student already exists with email: " + request.getEmail());
        });

        Student student = new Student();
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setTargetGoal(request.getTargetGoal());
        student.setDailyStudyHours(request.getDailyStudyHours());

        Student saved = studentRepository.save(student);
        log.info("Created student with id={} and email={}", saved.getId(), saved.getEmail());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long studentId, StudentRequest request) {
        Long safeStudentId = Objects.requireNonNull(studentId, "studentId must not be null");
        Student student = studentRepository.findById(safeStudentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        studentRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(studentId)) {
                throw new DuplicateResourceException("Another student already uses email: " + request.getEmail());
            }
        });

        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setTargetGoal(request.getTargetGoal());
        student.setDailyStudyHours(request.getDailyStudyHours());

        Student updated = studentRepository.save(student);
        log.info("Updated student profile for id={}", studentId);
        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long studentId) {
        Long safeStudentId = Objects.requireNonNull(studentId, "studentId must not be null");
        Student student = studentRepository.findById(safeStudentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        return toResponse(student);
    }

    private StudentResponse toResponse(Student student) {
        return StudentResponse.builder()
            .id(student.getId())
            .name(student.getName())
            .email(student.getEmail())
            .targetGoal(student.getTargetGoal())
            .dailyStudyHours(student.getDailyStudyHours())
            .build();
    }
}
