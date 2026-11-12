package com.capstone.adaptivelearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    private Long id;
    private String title;
    private String description;
    private String priority;          // "LOW", "MEDIUM", "HIGH"
    private String status;            // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    private String learningObjective; // Mục tiêu cụ thể cần đạt
    private String estimatedDuration; // Thời lượng dự kiến
    private String explanation;       // Lý do hệ thống đề xuất (Explainability)
    private String subjectCode;       // Mã môn học liên quan
    private String resourceType;      // Loại tài liệu học tập
}
