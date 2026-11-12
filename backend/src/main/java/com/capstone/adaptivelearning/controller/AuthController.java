package com.capstone.adaptivelearning.controller;

import com.capstone.adaptivelearning.dto.response.ApiResponse;
import com.capstone.adaptivelearning.dto.response.UserResponse;
import com.capstone.adaptivelearning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(ApiResponse.ok(null, "Chưa đăng nhập"));
        }
        UserResponse user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(user, "Lấy thông tin người dùng thành công"));
    }
}

