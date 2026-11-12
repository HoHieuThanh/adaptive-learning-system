package com.capstone.adaptivelearning.service;

import com.capstone.adaptivelearning.dto.request.SubjectRequest;
import com.capstone.adaptivelearning.dto.response.SubjectResponse;
import com.capstone.adaptivelearning.entity.Subject;
import com.capstone.adaptivelearning.exception.BadRequestException;
import com.capstone.adaptivelearning.exception.ResourceNotFoundException;
import com.capstone.adaptivelearning.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {
        return mapToResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Subject findEntityById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + id));
    }

    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        if (subjectRepository.existsBySubjectCode(request.getSubjectCode())) {
            throw new BadRequestException("Mã môn học đã tồn tại: " + request.getSubjectCode());
        }

        Subject subject = Subject.builder()
                .subjectCode(request.getSubjectCode().trim().toUpperCase())
                .subjectName(request.getSubjectName().trim())
                .build();

        return mapToResponse(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        Subject subject = findEntityById(id);

        if (!subject.getSubjectCode().equalsIgnoreCase(request.getSubjectCode())
                && subjectRepository.existsBySubjectCode(request.getSubjectCode())) {
            throw new BadRequestException("Mã môn học đã tồn tại: " + request.getSubjectCode());
        }

        subject.setSubjectCode(request.getSubjectCode().trim().toUpperCase());
        subject.setSubjectName(request.getSubjectName().trim());

        return mapToResponse(subjectRepository.save(subject));
    }

    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = findEntityById(id);
        subjectRepository.delete(subject);
    }

    public SubjectResponse mapToResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .subjectCode(subject.getSubjectCode())
                .subjectName(subject.getSubjectName())
                .build();
    }
}

