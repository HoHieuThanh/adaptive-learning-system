package com.capstone.adaptivelearning.service;

import com.capstone.adaptivelearning.dto.ml.MLPredictRequest;
import com.capstone.adaptivelearning.dto.ml.MLPredictResponse;
import com.capstone.adaptivelearning.dto.ml.MLStudentPayload;
import com.capstone.adaptivelearning.dto.request.PredictRequest;
import com.capstone.adaptivelearning.dto.response.PredictionResponse;
import com.capstone.adaptivelearning.entity.Grade;
import com.capstone.adaptivelearning.entity.LearningActivity;
import com.capstone.adaptivelearning.entity.Prediction;
import com.capstone.adaptivelearning.entity.Student;
import com.capstone.adaptivelearning.exception.ResourceNotFoundException;
import com.capstone.adaptivelearning.repository.GradeRepository;
import com.capstone.adaptivelearning.repository.LearningActivityRepository;
import com.capstone.adaptivelearning.repository.PredictionRepository;
import com.capstone.adaptivelearning.repository.StudentRepository;
import com.capstone.adaptivelearning.service.ml.MLServiceClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final LearningActivityRepository activityRepository;
    private final PredictionRepository predictionRepository;
    private final MLServiceClient mlServiceClient;
    private final LearningPathService learningPathService;

    @Transactional
    public PredictionResponse predictForStudent(Long studentId, PredictRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + studentId));

        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        LearningActivity activity = activityRepository.findFirstByStudentIdOrderByRecordedAtDesc(studentId)
                .orElse(null);

        // 1. Tính toán điểm số quy đổi sang thang 0-20 cho mô hình UCI
        double avgScore10 = 5.0; // Mặc định nếu chưa có điểm
        if (!grades.isEmpty()) {
            double sum = grades.stream()
                    .mapToDouble(g -> g.getScore().doubleValue())
                    .sum();
            avgScore10 = sum / grades.size();
        }
        // Thang 10 sang thang 20: nhân 2.0
        double g1Scaled = Math.min(20.0, Math.max(0.0, avgScore10 * 2.0));
        double g2Scaled = g1Scaled;

        // 2. Chuyển đổi giờ học thành studytime (1: <2h, 2: 2-5h, 3: 5-10h, 4: >10h)
        int studytime = 2;
        int absences = 4;
        if (activity != null) {
            double hours = activity.getStudyHours().doubleValue();
            if (hours < 2.0) studytime = 1;
            else if (hours <= 5.0) studytime = 2;
            else if (hours <= 10.0) studytime = 3;
            else studytime = 4;

            // Chuyên cần 100% -> 0 absences; 80% -> 4 absences; 50% -> 10 absences
            double attRate = activity.getAttendanceRate().doubleValue();
            absences = (int) Math.round((100.0 - attRate) * 0.2);
        }

        // 3. Xây dựng payload gọi sang FastAPI
        String chosenModel = (request != null && request.getModelName() != null && !request.getModelName().isBlank())
                ? request.getModelName().trim()
                : "xgboost";

        MLStudentPayload payload = MLStudentPayload.builder()
                .studentId(student.getStudentCode())
                .studytime(studytime)
                .absences(absences)
                .g1(g1Scaled)
                .g2(g2Scaled)
                .build();

        MLPredictRequest mlRequest = MLPredictRequest.builder()
                .student(payload)
                .modelName(chosenModel)
                .build();

        // 4. Gọi ML Service thật qua HTTP REST
        MLPredictResponse mlResponse = mlServiceClient.predict(mlRequest);

        // 5. Lưu kết quả dự đoán vào Database
        Prediction prediction = Prediction.builder()
                .student(student)
                .predictedLevel(mlResponse.getPredictedLevel())
                .confidence(BigDecimal.valueOf(mlResponse.getConfidence() != null ? mlResponse.getConfidence() : 1.0)
                        .setScale(4, RoundingMode.HALF_UP))
                .modelName(mlResponse.getModelName() != null ? mlResponse.getModelName() : chosenModel)
                .build();

        Prediction savedPrediction = predictionRepository.save(prediction);

        // 6. Kích hoạt Rule Engine sinh lộ trình thích ứng
        try {
            learningPathService.generateAdaptivePath(student, savedPrediction, grades, activity);
        } catch (Exception ex) {
            log.warn("Không thể sinh lộ trình thích ứng: {}", ex.getMessage());
        }

        return mapToResponse(savedPrediction);
    }

    @Transactional(readOnly = true)
    public List<PredictionResponse> getPredictionHistory(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Không tìm thấy sinh viên ID: " + studentId);
        }
        return predictionRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PredictionResponse> getAllPredictions() {
        return predictionRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PredictionResponse mapToResponse(Prediction prediction) {
        return PredictionResponse.builder()
                .id(prediction.getId())
                .studentId(prediction.getStudent().getId())
                .studentName(prediction.getStudent().getFullName())
                .studentCode(prediction.getStudent().getStudentCode())
                .predictedLevel(prediction.getPredictedLevel())
                .confidence(prediction.getConfidence())
                .modelName(prediction.getModelName())
                .createdAt(prediction.getCreatedAt())
                .build();
    }
}

