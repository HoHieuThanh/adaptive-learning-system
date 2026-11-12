package com.capstone.adaptivelearning.service;

import com.capstone.adaptivelearning.dto.request.GradeRequest;
import com.capstone.adaptivelearning.dto.response.GradeResponse;
import com.capstone.adaptivelearning.entity.Grade;
import com.capstone.adaptivelearning.entity.Student;
import com.capstone.adaptivelearning.entity.Subject;
import com.capstone.adaptivelearning.exception.ResourceNotFoundException;
import com.capstone.adaptivelearning.repository.GradeRepository;
import com.capstone.adaptivelearning.repository.StudentRepository;
import com.capstone.adaptivelearning.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<GradeResponse> getAllGrades() {
        return gradeRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + studentId);
        }
        return gradeRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GradeResponse getGradeById(Long id) {
        return mapToResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Grade findEntityById(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi điểm với ID: " + id));
    }

    @Transactional
    public GradeResponse createGrade(GradeRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên ID: " + request.getStudentId()));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học ID: " + request.getSubjectId()));

        Grade grade = Grade.builder()
                .student(student)
                .subject(subject)
                .score(request.getScore())
                .semester(request.getSemester().trim())
                .build();

        return mapToResponse(gradeRepository.save(grade));
    }

    @Transactional
    public GradeResponse updateGrade(Long id, GradeRequest request) {
        Grade grade = findEntityById(id);
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên ID: " + request.getStudentId()));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học ID: " + request.getSubjectId()));

        grade.setStudent(student);
        grade.setSubject(subject);
        grade.setScore(request.getScore());
        grade.setSemester(request.getSemester().trim());

        return mapToResponse(gradeRepository.save(grade));
    }

    @Transactional
    public void deleteGrade(Long id) {
        Grade grade = findEntityById(id);
        gradeRepository.delete(grade);
    }

    public GradeResponse mapToResponse(Grade grade) {
        return GradeResponse.builder()
                .id(grade.getId())
                .studentId(grade.getStudent().getId())
                .studentName(grade.getStudent().getFullName())
                .subjectId(grade.getSubject().getId())
                .subjectCode(grade.getSubject().getSubjectCode())
                .subjectName(grade.getSubject().getSubjectName())
                .score(grade.getScore())
                .semester(grade.getSemester())
                .build();
    }
}

