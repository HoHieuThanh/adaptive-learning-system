package com.capstone.adaptivelearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String title;
    private String description;
    private String learningGoal;
    private String estimatedWeeks;
    private String status; // "IN_PROGRESS", "COMPLETED", "ARCHIVED"
    private Integer progressPercentage;
    private Integer totalRecommendations;
    private Integer completedRecommendations;
    private LocalDateTime createdAt;
    private List<RecommendationResponse> recommendations;
}
