package com.capstone.adaptivelearning.controller;

import com.capstone.adaptivelearning.dto.request.SubjectRequest;
import com.capstone.adaptivelearning.dto.response.ApiResponse;
import com.capstone.adaptivelearning.dto.response.SubjectResponse;
import com.capstone.adaptivelearning.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubjects() {
        List<SubjectResponse> list = subjectService.getAllSubjects();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách môn học thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(@PathVariable Long id) {
        SubjectResponse subject = subjectService.getSubjectById(id);
        return ResponseEntity.ok(ApiResponse.ok(subject, "Lấy chi tiết môn học thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(@Valid @RequestBody SubjectRequest request) {
        SubjectResponse subject = subjectService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(subject, "Tạo môn học mới thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectRequest request
    ) {
        SubjectResponse subject = subjectService.updateSubject(id, request);
        return ResponseEntity.ok(ApiResponse.ok(subject, "Cập nhật môn học thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa môn học thành công"));
    }
}

