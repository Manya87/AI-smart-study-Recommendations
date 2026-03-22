package com.example.demo.repository;

import com.example.demo.entity.QuizResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByStudentId(Long studentId);
    List<QuizResult> findByStudentIdAndTopicId(Long studentId, Long topicId);
}
