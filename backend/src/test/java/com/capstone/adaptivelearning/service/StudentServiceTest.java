package com.capstone.adaptivelearning.service;

import com.capstone.adaptivelearning.dto.request.StudentRequest;
import com.capstone.adaptivelearning.dto.response.StudentResponse;
import com.capstone.adaptivelearning.entity.Student;
import com.capstone.adaptivelearning.exception.BadRequestException;
import com.capstone.adaptivelearning.exception.ResourceNotFoundException;
import com.capstone.adaptivelearning.repository.GradeRepository;
import com.capstone.adaptivelearning.repository.PredictionRepository;
import com.capstone.adaptivelearning.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private PredictionRepository predictionRepository;

    @InjectMocks
    private StudentService studentService;

    private Student mockStudent;

    @BeforeEach
    void setUp() {
        mockStudent = Student.builder()
                .id(1L)
                .studentCode("SV001")
                .fullName("Nguyễn Văn An")
                .email("an.nv@university.edu.vn")
                .className("CNTT-K65")
                .build();
    }

    @Test
    @DisplayName("Tìm sinh viên theo ID thành công")
    void testGetStudentById_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(gradeRepository.findByStudentId(1L)).thenReturn(Collections.emptyList());
        when(predictionRepository.findFirstByStudentIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());

        StudentResponse response = studentService.getStudentById(1L);

        assertNotNull(response);
        assertEquals("SV001", response.getStudentCode());
        assertEquals("Nguyễn Văn An", response.getFullName());
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Ném ngoại lệ khi sinh viên không tồn tại")
    void testGetStudentById_NotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(99L));
        verify(studentRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Tạo mới sinh viên thành công khi dữ liệu hợp lệ")
    void testCreateStudent_Success() {
        StudentRequest request = StudentRequest.builder()
                .studentCode("SV002")
                .fullName("Trần Thị Bình")
                .email("binh.tt@university.edu.vn")
                .className("CNTT-K65")
                .build();

        when(studentRepository.existsByStudentCode("SV002")).thenReturn(false);
        when(studentRepository.existsByEmail("binh.tt@university.edu.vn")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student s = invocation.getArgument(0);
            s.setId(2L);
            return s;
        });

        StudentResponse response = studentService.createStudent(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("SV002", response.getStudentCode());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi tạo sinh viên bị trùng mã sinh viên")
    void testCreateStudent_DuplicateStudentCode() {
        StudentRequest request = StudentRequest.builder()
                .studentCode("SV001")
                .fullName("Nguyễn Văn An")
                .email("unique@university.edu.vn")
                .className("CNTT-K65")
                .build();

        when(studentRepository.existsByStudentCode("SV001")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> studentService.createStudent(request));
        verify(studentRepository, never()).save(any(Student.class));
    }
}

