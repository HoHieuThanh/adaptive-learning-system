package com.capstone.adaptivelearning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLearningPathStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(
        regexp = "IN_PROGRESS|COMPLETED|ARCHIVED",
        message = "Trạng thái lộ trình hợp lệ gồm: IN_PROGRESS, COMPLETED, ARCHIVED"
    )
    private String status;
}

