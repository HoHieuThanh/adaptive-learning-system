package com.capstone.adaptivelearning.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictRequest {
    @Builder.Default
    private String modelName = "xgboost"; // "xgboost", "decision_tree", "kmeans_hybrid"
}

