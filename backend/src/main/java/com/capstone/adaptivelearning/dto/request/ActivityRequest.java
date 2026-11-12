package com.capstone.adaptivelearning.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class ActivityRequest {

    @NotNull(message = "ID sinh viên không được để trống")
    private Long studentId;

    @NotNull(message = "Số giờ tự học không được để trống")
    @DecimalMin(value = "0.0", message = "Giờ học tối thiểu là 0.0")
    @DecimalMax(value = "100.0", message = "Giờ học tối đa là 100.0")
    private BigDecimal studyHours;

    @NotNull(message = "Tỷ lệ chuyên cần không được để trống")
    @DecimalMin(value = "0.0", message = "Tỷ lệ chuyên cần tối thiểu là 0%")
    @DecimalMax(value = "100.0", message = "Tỷ lệ chuyên cần tối đa là 100%")
    private BigDecimal attendanceRate;

    @NotNull(message = "Tỷ lệ hoàn thành bài tập không được để trống")
    @DecimalMin(value = "0.0", message = "Tỷ lệ hoàn thành tối thiểu là 0%")
    @DecimalMax(value = "100.0", message = "Tỷ lệ hoàn thành tối đa là 100%")
    private BigDecimal assignmentCompletion;

    @NotNull(message = "Số lần đăng nhập LMS không được để trống")
    @Min(value = 0, message = "LMS logins không được âm")
    private Integer lmsLogins;
}

