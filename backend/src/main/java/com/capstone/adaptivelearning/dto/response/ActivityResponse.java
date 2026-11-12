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
public class ActivityResponse {
    private Long id;
    private Long studentId;
    private BigDecimal studyHours;
    private BigDecimal attendanceRate;
    private BigDecimal assignmentCompletion;
    private Integer lmsLogins;
    private LocalDateTime recordedAt;
}

