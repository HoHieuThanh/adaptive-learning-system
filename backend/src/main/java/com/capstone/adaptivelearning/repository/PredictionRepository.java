package com.capstone.adaptivelearning.repository;

import com.capstone.adaptivelearning.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    Optional<Prediction> findFirstByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Query("SELECT p.predictedLevel, COUNT(p) FROM Prediction p GROUP BY p.predictedLevel")
    List<Object[]> countByPredictedLevelGroup();
}

