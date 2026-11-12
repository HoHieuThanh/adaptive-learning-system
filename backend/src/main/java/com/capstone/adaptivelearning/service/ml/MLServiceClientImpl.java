package com.capstone.adaptivelearning.service.ml;

import com.capstone.adaptivelearning.dto.ml.MLPredictRequest;
import com.capstone.adaptivelearning.dto.ml.MLPredictResponse;
import com.capstone.adaptivelearning.exception.MLServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Primary
public class MLServiceClientImpl implements MLServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MLServiceClientImpl.class);

    private final RestClient restClient;
    private final String mlServiceUrl;

    public MLServiceClientImpl(
            RestClient.Builder restClientBuilder,
            @Value("${ml-service.url:http://localhost:8000}") String mlServiceUrl
    ) {
        this.mlServiceUrl = mlServiceUrl;
        this.restClient = restClientBuilder
                .baseUrl(mlServiceUrl)
                .build();
    }

    @Override
    public MLPredictResponse predict(MLPredictRequest request) {
        log.info("Gửi yêu cầu dự đoán tới ML Service tại {}/predict cho student_id={}", 
                mlServiceUrl, request.getStudent() != null ? request.getStudent().getStudentId() : "N/A");
        try {
            return restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MLPredictResponse.class);
        } catch (Exception ex) {
            log.error("Không thể kết nối hoặc lỗi từ ML Service: {}", ex.getMessage());
            throw new MLServiceException("Không thể kết nối tới ML Service tại " + mlServiceUrl + ". Vui lòng kiểm tra lại dịch vụ FastAPI: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isServiceHealthy() {
        try {
            Map<?, ?> response = restClient.get()
                    .uri("/health")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            return response != null && "UP".equalsIgnoreCase(String.valueOf(response.get("status")));
        } catch (Exception ex) {
            log.warn("Health check ML Service thất bại: {}", ex.getMessage());
            return false;
        }
    }
}

