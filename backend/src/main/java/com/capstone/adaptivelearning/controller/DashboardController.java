package com.capstone.adaptivelearning.controller;

import com.capstone.adaptivelearning.dto.response.ApiResponse;
import com.capstone.adaptivelearning.dto.response.DashboardStatsResponse;
import com.capstone.adaptivelearning.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getStats();
        return ResponseEntity.ok(ApiResponse.ok(stats, "Lấy thống kê bảng điều khiển thành công"));
    }
}

