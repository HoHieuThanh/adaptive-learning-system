package com.capstone.adaptivelearning.service;

import com.capstone.adaptivelearning.dto.response.DashboardStatsResponse;
import com.capstone.adaptivelearning.entity.Grade;
import com.capstone.adaptivelearning.repository.GradeRepository;
import com.capstone.adaptivelearning.repository.PredictionRepository;
import com.capstone.adaptivelearning.repository.StudentRepository;
import com.capstone.adaptivelearning.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final PredictionRepository predictionRepository;
    private final GradeRepository gradeRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long totalStudents = studentRepository.count();
        long totalSubjects = subjectRepository.count();
        long totalPredictions = predictionRepository.count();

        List<Object[]> levelCounts = predictionRepository.countByPredictedLevelGroup();
        Map<String, Long> countMap = new HashMap<>();
        long atRisk = 0;
        long moderate = 0;
        long high = 0;

        for (Object[] row : levelCounts) {
            String level = String.valueOf(row[0]);
            Long cnt = (Long) row[1];
            countMap.put(level, cnt);

            if ("Low".equalsIgnoreCase(level) || "AT_RISK".equalsIgnoreCase(level)) {
                atRisk += cnt;
            } else if ("Medium".equalsIgnoreCase(level) || "MODERATE".equalsIgnoreCase(level)) {
                moderate += cnt;
            } else if ("High".equalsIgnoreCase(level)) {
                high += cnt;
            }
        }

        // Tính điểm trung bình toàn hệ thống
        List<Grade> grades = gradeRepository.findAll();
        BigDecimal avgScore = BigDecimal.ZERO;
        if (!grades.isEmpty()) {
            BigDecimal sum = grades.stream()
                    .map(Grade::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgScore = sum.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
        }

        return DashboardStatsResponse.builder()
                .totalStudents(totalStudents)
                .totalSubjects(totalSubjects)
                .totalPredictions(totalPredictions)
                .atRiskCount(atRisk)
                .moderateCount(moderate)
                .highCount(high)
                .averageSystemScore(avgScore)
                .predictionsByLevel(countMap)
                .build();
    }
}

