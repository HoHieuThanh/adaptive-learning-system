package com.capstone.adaptivelearning.service;

import com.capstone.adaptivelearning.dto.request.StudentRequest;
import com.capstone.adaptivelearning.dto.response.StudentResponse;
import com.capstone.adaptivelearning.entity.Grade;
import com.capstone.adaptivelearning.entity.Prediction;
import com.capstone.adaptivelearning.entity.Student;
import com.capstone.adaptivelearning.exception.BadRequestException;
import com.capstone.adaptivelearning.exception.ResourceNotFoundException;
import com.capstone.adaptivelearning.repository.GradeRepository;
import com.capstone.adaptivelearning.repository.PredictionRepository;
import com.capstone.adaptivelearning.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final PredictionRepository predictionRepository;

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = findEntityById(id);
        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public Student findEntityById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + id));
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw new BadRequestException("Mã sinh viên đã tồn tại: " + request.getStudentCode());
        }
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng: " + request.getEmail());
        }

        Student student = Student.builder()
                .studentCode(request.getStudentCode().trim())
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .className(request.getClassName().trim())
                .build();

        return mapToResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findEntityById(id);

        if (!student.getStudentCode().equalsIgnoreCase(request.getStudentCode())
                && studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw new BadRequestException("Mã sinh viên đã tồn tại: " + request.getStudentCode());
        }
        if (!student.getEmail().equalsIgnoreCase(request.getEmail())
                && studentRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng: " + request.getEmail());
        }

        student.setStudentCode(request.getStudentCode().trim());
        student.setFullName(request.getFullName().trim());
        student.setEmail(request.getEmail().trim().toLowerCase());
        student.setClassName(request.getClassName().trim());

        return mapToResponse(studentRepository.save(student));
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = findEntityById(id);
        studentRepository.delete(student);
    }

    public StudentResponse mapToResponse(Student student) {
        List<Grade> grades = gradeRepository.findByStudentId(student.getId());
        BigDecimal avgScore = BigDecimal.ZERO;
        if (!grades.isEmpty()) {
            BigDecimal sum = grades.stream()
                    .map(Grade::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgScore = sum.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
        }

        Prediction latestPred = predictionRepository.findFirstByStudentIdOrderByCreatedAtDesc(student.getId())
                .orElse(null);

        return StudentResponse.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .className(student.getClassName())
                .averageScore(avgScore)
                .latestPredictedLevel(latestPred != null ? latestPred.getPredictedLevel() : "CHƯA_DỰ_ĐOÁN")
                .latestConfidence(latestPred != null ? latestPred.getConfidence() : null)
                .build();
    }
}

