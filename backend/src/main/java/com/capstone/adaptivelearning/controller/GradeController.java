package com.capstone.adaptivelearning.controller;

import com.capstone.adaptivelearning.dto.request.GradeRequest;
import com.capstone.adaptivelearning.dto.response.ApiResponse;
import com.capstone.adaptivelearning.dto.response.GradeResponse;
import com.capstone.adaptivelearning.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getAllGrades() {
        List<GradeResponse> list = gradeService.getAllGrades();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách điểm số thành công"));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getGradesByStudent(@PathVariable Long studentId) {
        List<GradeResponse> list = gradeService.getGradesByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách điểm số của sinh viên thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GradeResponse>> getGradeById(@PathVariable Long id) {
        GradeResponse grade = gradeService.getGradeById(id);
        return ResponseEntity.ok(ApiResponse.ok(grade, "Lấy chi tiết điểm số thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GradeResponse>> createGrade(@Valid @RequestBody GradeRequest request) {
        GradeResponse grade = gradeService.createGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(grade, "Thêm bản ghi điểm thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GradeResponse>> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody GradeRequest request
    ) {
        GradeResponse grade = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(ApiResponse.ok(grade, "Cập nhật điểm số thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa bản ghi điểm thành công"));
    }
}

