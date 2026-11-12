package com.capstone.adaptivelearning.controller;

import com.capstone.adaptivelearning.dto.request.StudentRequest;
import com.capstone.adaptivelearning.dto.response.ApiResponse;
import com.capstone.adaptivelearning.dto.response.StudentResponse;
import com.capstone.adaptivelearning.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents() {
        List<StudentResponse> list = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách sinh viên thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        StudentResponse student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.ok(student, "Lấy chi tiết sinh viên thành công"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse student = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(student, "Tạo sinh viên mới thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request
    ) {
        StudentResponse student = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.ok(student, "Cập nhật sinh viên thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Xóa sinh viên thành công"));
    }
}

