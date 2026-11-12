package com.capstone.adaptivelearning.service.ml;

import com.capstone.adaptivelearning.dto.ml.MLPredictRequest;
import com.capstone.adaptivelearning.dto.ml.MLPredictResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("mockMLServiceClient")
public class MockMLServiceClient implements MLServiceClient {

    @Override
    public MLPredictResponse predict(MLPredictRequest request) {
        String level = "Medium";
        double g1 = request.getStudent() != null && request.getStudent().getG1() != null ? request.getStudent().getG1() : 10.0;
        if (g1 < 10.0) {
            level = "Low";
        } else if (g1 >= 15.0) {
            level = "High";
        }

        return MLPredictResponse.builder()
                .studentId(request.getStudent() != null ? request.getStudent().getStudentId() : "mock_id")
                .predictedLevel(level)
                .modelName(request.getModelName() != null ? request.getModelName() : "mock_model")
                .confidence(0.92)
                .clusterId(1)
                .probabilities(Map.of("Low", 0.05, "Medium", 0.85, "High", 0.10))
                .build();
    }

    @Override
    public boolean isServiceHealthy() {
        return true;
    }
}

