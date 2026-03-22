package com.example.demo.repository;

import com.example.demo.entity.Recommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByStudentIdOrderByPriorityAsc(Long studentId);
    void deleteByStudentId(Long studentId);
}
