package com.example.demo.repository;

import com.example.demo.entity.Topic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findBySubjectId(Long subjectId);
}
