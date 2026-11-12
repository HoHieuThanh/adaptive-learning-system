package com.capstone.adaptivelearning.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLPredictRequest {

    private MLStudentPayload student;

    @Builder.Default
    @JsonProperty("model_name")
    private String modelName = "xgboost";
}

