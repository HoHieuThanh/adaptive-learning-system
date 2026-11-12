package com.capstone.adaptivelearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    private long totalStudents;
    private long totalSubjects;
    private long totalPredictions;
    private long atRiskCount;       // Low level
    private long moderateCount;     // Medium level
    private long highCount;         // High level
    private BigDecimal averageSystemScore;
    private Map<String, Long> predictionsByLevel;
}

