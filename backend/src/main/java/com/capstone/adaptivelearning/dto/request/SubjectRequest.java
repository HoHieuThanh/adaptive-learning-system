package com.capstone.adaptivelearning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectRequest {

    @NotBlank(message = "Mã môn học không được để trống")
    @Size(max = 20, message = "Mã môn học tối đa 20 ký tự")
    private String subjectCode;

    @NotBlank(message = "Tên môn học không được để trống")
    @Size(max = 100, message = "Tên môn học tối đa 100 ký tự")
    private String subjectName;
}

