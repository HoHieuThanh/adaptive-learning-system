package com.capstone.adaptivelearning.controller;

import com.capstone.adaptivelearning.dto.request.UpdateLearningPathStatusRequest;
import com.capstone.adaptivelearning.dto.request.UpdateRecommendationStatusRequest;
import com.capstone.adaptivelearning.dto.response.ApiResponse;
import com.capstone.adaptivelearning.dto.response.LearningPathResponse;
import com.capstone.adaptivelearning.dto.response.RecommendationResponse;
import com.capstone.adaptivelearning.service.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService learningPathService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<LearningPathResponse>>> getLearningPathsByStudent(@PathVariable Long studentId) {
        List<LearningPathResponse> paths = learningPathService.getLearningPathsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.ok(paths, "Lấy danh sách lộ trình học tập thành công"));
    }

    @GetMapping("/student/{studentId}/latest")
    public ResponseEntity<ApiResponse<LearningPathResponse>> getLatestLearningPath(@PathVariable Long studentId) {
        LearningPathResponse path = learningPathService.getLatestLearningPath(studentId);
        return ResponseEntity.ok(ApiResponse.ok(path, "Lấy lộ trình học tập mới nhất thành công"));
    }

    @PostMapping("/generate/{studentId}")
    public ResponseEntity<ApiResponse<LearningPathResponse>> generateOrRefreshPath(@PathVariable Long studentId) {
        LearningPathResponse path = learningPathService.generateOrRefreshPath(studentId);
        return ResponseEntity.ok(ApiResponse.ok(path, "Tạo hoặc làm mới lộ trình thích ứng thành công"));
    }

    @PatchMapping("/recommendations/{recommendationId}/status")
    public ResponseEntity<ApiResponse<RecommendationResponse>> updateRecommendationStatus(
            @PathVariable Long recommendationId,
            @Valid @RequestBody UpdateRecommendationStatusRequest request
    ) {
        RecommendationResponse updated = learningPathService.updateRecommendationStatus(recommendationId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(updated, "Cập nhật tiến độ đề xuất học tập thành công"));
    }

    @PatchMapping("/{pathId}/status")
    public ResponseEntity<ApiResponse<LearningPathResponse>> updateLearningPathStatus(
            @PathVariable Long pathId,
            @Valid @RequestBody UpdateLearningPathStatusRequest request
    ) {
        LearningPathResponse updated = learningPathService.updateLearningPathStatus(pathId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(updated, "Cập nhật trạng thái lộ trình học tập thành công"));
    }
}
