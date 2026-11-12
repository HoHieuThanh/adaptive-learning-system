package com.capstone.adaptivelearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {
    private Long id;
    private String studentCode;
    private String fullName;
    private String email;
    private String className;
    private BigDecimal averageScore;
    private String latestPredictedLevel;
    private BigDecimal latestConfidence;
}

