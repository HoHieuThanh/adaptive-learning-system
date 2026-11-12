package com.capstone.adaptivelearning.controller;

import com.capstone.adaptivelearning.dto.request.StudentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    @Test
    @DisplayName("GET /api/health trả về status UP mà không cần xác thực")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/students thành công với tài khoản advisor")
    void testGetAllStudents_WithAdvisorRole() throws Exception {
        mockMvc.perform(get("/api/students")
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader("advisor", "advisor123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /api/students bị từ chối 403 Forbidden nếu là tài khoản student")
    void testCreateStudent_ForbiddenForStudent() throws Exception {
        StudentRequest request = StudentRequest.builder()
                .studentCode("SV999")
                .fullName("Test Student")
                .email("test@edu.vn")
                .className("CNTT")
                .build();

        mockMvc.perform(post("/api/students")
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader("student", "student123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/students trả về 400 Validation Error khi thiếu thông tin bắt buộc")
    void testCreateStudent_ValidationError() throws Exception {
        StudentRequest invalidRequest = StudentRequest.builder()
                .studentCode("")
                .fullName("")
                .email("not-an-email")
                .className("")
                .build();

        mockMvc.perform(post("/api/students")
                        .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.studentCode").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }
}
