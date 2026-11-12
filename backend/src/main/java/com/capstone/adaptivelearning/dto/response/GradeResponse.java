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
public class GradeResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private BigDecimal score;
    private String semester;
}

