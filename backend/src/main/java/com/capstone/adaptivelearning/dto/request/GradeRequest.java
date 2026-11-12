package com.capstone.adaptivelearning.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRequest {

    @NotNull(message = "ID sinh viên không được để trống")
    private Long studentId;

    @NotNull(message = "ID môn học không được để trống")
    private Long subjectId;

    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.00", message = "Điểm số tối thiểu là 0.00")
    @DecimalMax(value = "20.00", message = "Điểm số tối đa là 20.00")
    private BigDecimal score;

    @NotBlank(message = "Học kỳ không được để trống (ví dụ: HK1-2026)")
    private String semester;
}

