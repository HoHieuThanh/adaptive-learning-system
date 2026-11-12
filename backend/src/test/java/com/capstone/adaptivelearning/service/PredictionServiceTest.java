package com.capstone.adaptivelearning.service;

import com.capstone.adaptivelearning.dto.ml.MLPredictRequest;
import com.capstone.adaptivelearning.dto.ml.MLPredictResponse;
import com.capstone.adaptivelearning.dto.request.PredictRequest;
import com.capstone.adaptivelearning.dto.response.PredictionResponse;
import com.capstone.adaptivelearning.entity.Grade;
import com.capstone.adaptivelearning.entity.LearningActivity;
import com.capstone.adaptivelearning.entity.Prediction;
import com.capstone.adaptivelearning.entity.Student;
import com.capstone.adaptivelearning.repository.GradeRepository;
import com.capstone.adaptivelearning.repository.LearningActivityRepository;
import com.capstone.adaptivelearning.repository.PredictionRepository;
import com.capstone.adaptivelearning.repository.StudentRepository;
import com.capstone.adaptivelearning.service.ml.MLServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private LearningActivityRepository activityRepository;

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private MLServiceClient mlServiceClient;

    @Mock
    private LearningPathService learningPathService;

    @InjectMocks
    private PredictionService predictionService;

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
    @DisplayName("Dự đoán trình độ học tập qua ML Service thành công")
    void testPredictForStudent_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(gradeRepository.findByStudentId(1L)).thenReturn(Collections.emptyList());
        when(activityRepository.findFirstByStudentIdOrderByRecordedAtDesc(1L)).thenReturn(Optional.empty());

        MLPredictResponse mockMLResponse = MLPredictResponse.builder()
                .studentId("SV001")
                .predictedLevel("High")
                .confidence(0.9520)
                .modelName("XGBoost")
                .probabilities(Map.of("Low", 0.01, "Medium", 0.04, "High", 0.95))
                .build();

        when(mlServiceClient.predict(any(MLPredictRequest.class))).thenReturn(mockMLResponse);
        when(predictionRepository.save(any(Prediction.class))).thenAnswer(invocation -> {
            Prediction p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        PredictionResponse response = predictionService.predictForStudent(1L, new PredictRequest("xgboost"));

        assertNotNull(response);
        assertEquals("High", response.getPredictedLevel());
        assertEquals("XGBoost", response.getModelName());
        assertEquals(BigDecimal.valueOf(0.9520).setScale(4), response.getConfidence());

        verify(mlServiceClient, times(1)).predict(any(MLPredictRequest.class));
        verify(predictionRepository, times(1)).save(any(Prediction.class));
        verify(learningPathService, times(1)).generateAdaptivePath(any(), any(), any(), any());
    }
}

