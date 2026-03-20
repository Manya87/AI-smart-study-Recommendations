package com.example.demo.repository;

import com.example.demo.entity.ResourceLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceLinkRepository extends JpaRepository<ResourceLink, Long> {
    List<ResourceLink> findByTopicId(Long topicId);
}
