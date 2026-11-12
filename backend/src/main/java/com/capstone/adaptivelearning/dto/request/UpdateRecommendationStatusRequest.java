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
public class UpdateRecommendationStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(
        regexp = "NOT_STARTED|IN_PROGRESS|COMPLETED|PENDING",
        message = "Trạng thái hợp lệ gồm: NOT_STARTED, IN_PROGRESS, COMPLETED"
    )
    private String status;
}

