package com.capstone.adaptivelearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectResponse {
    private Long id;
    private String subjectCode;
    private String subjectName;
}

