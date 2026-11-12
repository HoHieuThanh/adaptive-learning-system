package com.capstone.adaptivelearning.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLPredictResponse {

    @JsonProperty("student_id")
    private String studentId;

    @JsonProperty("predicted_level")
    private String predictedLevel;

    @JsonProperty("model_name")
    private String modelName;

    private Double confidence;

    @JsonProperty("cluster_id")
    private Integer clusterId;

    private Map<String, Double> probabilities;
}

