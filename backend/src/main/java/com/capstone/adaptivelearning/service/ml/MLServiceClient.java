package com.capstone.adaptivelearning.service.ml;

import com.capstone.adaptivelearning.dto.ml.MLPredictRequest;
import com.capstone.adaptivelearning.dto.ml.MLPredictResponse;

public interface MLServiceClient {
    MLPredictResponse predict(MLPredictRequest request);
    boolean isServiceHealthy();
}

