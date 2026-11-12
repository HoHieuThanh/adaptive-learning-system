package com.capstone.adaptivelearning.repository;

import com.capstone.adaptivelearning.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    List<LearningPath> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    Optional<LearningPath> findFirstByStudentIdOrderByCreatedAtDesc(Long studentId);
}

