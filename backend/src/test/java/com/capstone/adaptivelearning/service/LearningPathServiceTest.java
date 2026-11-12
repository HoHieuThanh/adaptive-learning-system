package com.capstone.adaptivelearning.service;

import com.capstone.adaptivelearning.dto.response.RecommendationResponse;
import com.capstone.adaptivelearning.entity.*;
import com.capstone.adaptivelearning.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningPathServiceTest {

    @Mock
    private LearningPathRepository learningPathRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private LearningActivityRepository learningActivityRepository;

    @Mock
    private PredictionRepository predictionRepository;

    @InjectMocks
    private LearningPathService learningPathService;

    @Test
    @DisplayName("Quy tắc sinh viên Low: Ưu tiên môn điểm < 5.0, kiến thức nền tảng, có mục tiêu và lý do giải thích")
    void testGenerateAdaptivePath_Low() {
        Student student = Student.builder()
                .id(3L)
                .studentCode("SV003")
                .fullName("Lê Hoàng Cường")
                .build();

        Prediction prediction = Prediction.builder()
                .id(1L)
                .student(student)
                .predictedLevel("Low")
                .confidence(BigDecimal.valueOf(0.9410))
                .modelName("XGBoost")
                .build();

        Subject cs101 = Subject.builder().id(1L).subjectCode("CS101").subjectName("Nhập môn Lập trình").build();
        Grade lowGrade = Grade.builder().student(student).subject(cs101).score(BigDecimal.valueOf(4.0)).semester("HK1").build();

        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LearningPath result = learningPathService.generateAdaptivePath(student, prediction, List.of(lowGrade), null);

        assertNotNull(result);
        assertTrue(result.getTitle().contains("Nguy cơ") || result.getTitle().contains("Can thiệp"));
        assertNotNull(result.getLearningGoal());
        assertNotNull(result.getEstimatedWeeks());

        // Kiểm tra đề xuất môn CS101 điểm thấp
        Recommendation cs101Rec = result.getRecommendations().stream()
                .filter(r -> "CS101".equals(r.getSubjectCode()))
                .findFirst()
                .orElse(null);

        assertNotNull(cs101Rec, "Phải có đề xuất riêng cho môn CS101 điểm dưới 5.0");
        assertEquals("HIGH", cs101Rec.getPriority(), "Môn điểm dưới 5.0 của sinh viên Low phải có ưu tiên HIGH");
        assertNotNull(cs101Rec.getLearningObjective(), "Phải có mục tiêu học tập rõ ràng");
        assertNotNull(cs101Rec.getEstimatedDuration(), "Phải có thời lượng dự kiến");
        assertNotNull(cs101Rec.getExplanation(), "Phải có giải thích lý do đề xuất");
        assertTrue(cs101Rec.getExplanation().contains("4.0") || cs101Rec.getExplanation().contains("Low"), "Lý do phải nêu rõ điểm số và trình độ");
    }

    @Test
    @DisplayName("Quy tắc sinh viên Medium: Đề xuất luyện tập chuyên đề còn yếu và bài tập vận dụng")
    void testGenerateAdaptivePath_Medium() {
        Student student = Student.builder()
                .id(2L)
                .studentCode("SV002")
                .fullName("Trần Thị Bình")
                .build();

        Prediction prediction = Prediction.builder()
                .id(2L)
                .student(student)
                .predictedLevel("Medium")
                .confidence(BigDecimal.valueOf(0.8850))
                .modelName("XGBoost")
                .build();

        Subject cs201 = Subject.builder().id(2L).subjectCode("CS201").subjectName("Cơ sở Dữ liệu").build();
        Grade mediumGrade = Grade.builder().student(student).subject(cs201).score(BigDecimal.valueOf(6.0)).semester("HK1").build();

        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LearningPath result = learningPathService.generateAdaptivePath(student, prediction, List.of(mediumGrade), null);

        assertNotNull(result);
        assertTrue(result.getTitle().contains("Tăng tốc") || result.getTitle().contains("Nâng cao"));
        assertFalse(result.getRecommendations().isEmpty());

        Recommendation rec = result.getRecommendations().stream()
                .filter(r -> "CS201".equals(r.getSubjectCode()))
                .findFirst()
                .orElse(null);

        assertNotNull(rec);
        assertTrue(rec.getTitle().contains("chuyên đề còn yếu") || rec.getTitle().contains("bài tập"));
        assertNotNull(rec.getLearningObjective());
        assertNotNull(rec.getExplanation());
    }

    @Test
    @DisplayName("Quy tắc sinh viên High: Đề xuất nội dung nâng cao, bài tập thử thách và đề tài NCKH")
    void testGenerateAdaptivePath_High() {
        Student student = Student.builder()
                .id(1L)
                .studentCode("SV001")
                .fullName("Nguyễn Văn An")
                .build();

        Prediction prediction = Prediction.builder()
                .id(3L)
                .student(student)
                .predictedLevel("High")
                .confidence(BigDecimal.valueOf(0.9600))
                .modelName("XGBoost")
                .build();

        Subject cs102 = Subject.builder().id(3L).subjectCode("CS102").subjectName("Cấu trúc Dữ liệu & Giải thuật").build();
        Grade highGrade = Grade.builder().student(student).subject(cs102).score(BigDecimal.valueOf(9.0)).semester("HK1").build();

        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LearningPath result = learningPathService.generateAdaptivePath(student, prediction, List.of(highGrade), null);

        assertNotNull(result);
        assertTrue(result.getTitle().contains("Phát triển") || result.getTitle().contains("Nghiên cứu"));

        // Kiểm tra đề xuất nghiên cứu khoa học hoặc thử thách chuyên sâu
        boolean hasResearchOrAdvanced = result.getRecommendations().stream()
                .anyMatch(r -> r.getTitle().contains("Thử thách") || r.getTitle().contains("Nghiên cứu") || r.getTitle().contains("Peer Tutor"));
        assertTrue(hasResearchOrAdvanced);
    }

    @Test
    @DisplayName("Cập nhật tiến độ: Chuyển trạng thái đề xuất sang COMPLETED và tự động cập nhật lộ trình")
    void testUpdateRecommendationStatus() {
        LearningPath path = LearningPath.builder()
                .id(10L)
                .status("IN_PROGRESS")
                .recommendations(new ArrayList<>())
                .build();

        Recommendation rec1 = Recommendation.builder()
                .id(101L)
                .learningPath(path)
                .title("Đề xuất 1")
                .status("NOT_STARTED")
                .build();

        path.getRecommendations().add(rec1);

        when(recommendationRepository.findById(101L)).thenReturn(Optional.of(rec1));
        when(recommendationRepository.save(any(Recommendation.class))).thenAnswer(inv -> inv.getArgument(0));

        RecommendationResponse response = learningPathService.updateRecommendationStatus(101L, "COMPLETED");

        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertEquals("COMPLETED", path.getStatus(), "Khi tất cả đề xuất hoàn thành, lộ trình phải chuyển sang COMPLETED");
    }
}
