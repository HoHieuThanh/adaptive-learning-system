package com.capstone.adaptivelearning.controller;

import com.capstone.adaptivelearning.dto.request.PredictRequest;
import com.capstone.adaptivelearning.dto.response.ApiResponse;
import com.capstone.adaptivelearning.dto.response.PredictionResponse;
import com.capstone.adaptivelearning.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/predict/{studentId}")
    public ResponseEntity<ApiResponse<PredictionResponse>> predictForStudent(
            @PathVariable Long studentId,
            @RequestBody(required = false) PredictRequest request
    ) {
        if (request == null) {
            request = new PredictRequest("xgboost");
        }
        PredictionResponse response = predictionService.predictForStudent(studentId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Thực hiện dự đoán và sinh lộ trình thích ứng thành công"));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<PredictionResponse>>> getPredictionHistory(@PathVariable Long studentId) {
        List<PredictionResponse> history = predictionService.getPredictionHistory(studentId);
        return ResponseEntity.ok(ApiResponse.ok(history, "Lấy lịch sử dự đoán của sinh viên thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PredictionResponse>>> getAllPredictions() {
        List<PredictionResponse> list = predictionService.getAllPredictions();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy toàn bộ lịch sử dự đoán hệ thống thành công"));
    }
}

