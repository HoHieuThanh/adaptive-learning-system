package com.capstone.adaptivelearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String predictedLevel; // "Low", "Medium", "High"
    private BigDecimal confidence;
    private String modelName;
    private LocalDateTime createdAt;
}

