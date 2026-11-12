package com.capstone.adaptivelearning.repository;

import com.capstone.adaptivelearning.entity.LearningActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningActivityRepository extends JpaRepository<LearningActivity, Long> {
    List<LearningActivity> findByStudentIdOrderByRecordedAtDesc(Long studentId);
    Optional<LearningActivity> findFirstByStudentIdOrderByRecordedAtDesc(Long studentId);
}

