package com.capstone.adaptivelearning.service;

import com.capstone.adaptivelearning.dto.response.LearningPathResponse;
import com.capstone.adaptivelearning.dto.response.RecommendationResponse;
import com.capstone.adaptivelearning.entity.*;
import com.capstone.adaptivelearning.exception.ResourceNotFoundException;
import com.capstone.adaptivelearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final RecommendationRepository recommendationRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final LearningActivityRepository learningActivityRepository;
    private final PredictionRepository predictionRepository;

    @Transactional(readOnly = true)
    public List<LearningPathResponse> getLearningPathsByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Không tìm thấy sinh viên ID: " + studentId);
        }
        return learningPathRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LearningPathResponse getLatestLearningPath(Long studentId) {
        LearningPath path = learningPathRepository.findFirstByStudentIdOrderByCreatedAtDesc(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa có lộ trình học tập nào cho sinh viên ID: " + studentId));
        return mapToResponse(path);
    }

    /**
     * Tạo hoặc làm mới lộ trình thích ứng dựa trên dữ liệu hiện có trong Database
     */
    @Transactional
    public LearningPathResponse generateOrRefreshPath(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên ID: " + studentId));

        Prediction latestPrediction = predictionRepository.findFirstByStudentIdOrderByCreatedAtDesc(studentId)
                .orElse(null);

        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        LearningActivity activity = learningActivityRepository.findFirstByStudentIdOrderByRecordedAtDesc(studentId)
                .orElse(null);

        LearningPath path = generateAdaptivePath(student, latestPrediction, grades, activity);
        return mapToResponse(path);
    }

    /**
     * Cập nhật trạng thái của một khuyến nghị học tập (NOT_STARTED, IN_PROGRESS, COMPLETED)
     */
    @Transactional
    public RecommendationResponse updateRecommendationStatus(Long recommendationId, String newStatus) {
        Recommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề xuất học tập ID: " + recommendationId));

        // Chuẩn hóa trạng thái
        String normalizedStatus = "PENDING".equalsIgnoreCase(newStatus) ? "NOT_STARTED" : newStatus.toUpperCase();
        rec.setStatus(normalizedStatus);
        recommendationRepository.save(rec);

        // Kiểm tra xem tất cả các đề xuất của lộ trình này đã hoàn thành chưa
        LearningPath path = rec.getLearningPath();
        if (path != null && path.getRecommendations() != null && !path.getRecommendations().isEmpty()) {
            boolean allCompleted = path.getRecommendations().stream()
                    .allMatch(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()));
            if (allCompleted) {
                path.setStatus("COMPLETED");
                learningPathRepository.save(path);
            } else if ("COMPLETED".equalsIgnoreCase(path.getStatus())) {
                path.setStatus("IN_PROGRESS");
                learningPathRepository.save(path);
            }
        }

        return mapToRecResponse(rec);
    }

    /**
     * Cập nhật trạng thái của cả lộ trình (IN_PROGRESS, COMPLETED, ARCHIVED)
     */
    @Transactional
    public LearningPathResponse updateLearningPathStatus(Long pathId, String newStatus) {
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lộ trình ID: " + pathId));

        path.setStatus(newStatus.toUpperCase());
        LearningPath updated = learningPathRepository.save(path);
        return mapToResponse(updated);
    }

    /**
     * Rule-Based Recommendation Engine:
     * Dựa trên trình độ AI (Low, Medium, High) kết hợp với điểm số từng môn để đề xuất nội dung thích ứng.
     */
    @Transactional
    public LearningPath generateAdaptivePath(
            Student student,
            Prediction prediction,
            List<Grade> grades,
            LearningActivity activity
    ) {
        String level = (prediction != null && prediction.getPredictedLevel() != null)
                ? prediction.getPredictedLevel()
                : "Medium";

        String title;
        String description;
        String learningGoal;
        String estimatedWeeks;

        LearningPath learningPath = LearningPath.builder()
                .student(student)
                .status("IN_PROGRESS")
                .recommendations(new ArrayList<>())
                .build();

        List<Recommendation> recs = new ArrayList<>();

        // Sắp xếp điểm số tăng dần: môn điểm thấp nhất được đưa lên đầu danh sách xem xét
        List<Grade> sortedGrades = new ArrayList<>(grades);
        sortedGrades.sort(Comparator.comparing(Grade::getScore));

        if ("Low".equalsIgnoreCase(level) || "AT_RISK".equalsIgnoreCase(level)) {
            title = "Lộ trình Can thiệp & Củng cố Nền tảng (Nguy cơ Cần Hỗ trợ)";
            description = String.format("Mô hình AI (%s) nhận diện sinh viên %s thuộc nhóm Cần hỗ trợ (Độ tin cậy %.1f%%). Tập trung vào kiến thức cơ bản và lấp lỗ hổng môn học.",
                    prediction != null ? prediction.getModelName() : "XGBoost",
                    student.getFullName(),
                    prediction != null && prediction.getConfidence() != null ? prediction.getConfidence().doubleValue() * 100 : 95.0);
            learningGoal = "Phòng tránh rớt môn, cải thiện các học phần dưới 5.0 lên mức trung bình khá (>= 5.5) và thiết lập kỷ luật tự học.";
            estimatedWeeks = "6 tuần";

            // 1. Phân tích từng môn học: Ưu tiên môn có điểm < 5.0
            for (Grade g : sortedGrades) {
                String subName = g.getSubject().getSubjectName();
                String subCode = g.getSubject().getSubjectCode();
                BigDecimal score = g.getScore();

                if (score.compareTo(BigDecimal.valueOf(5.0)) < 0) {
                    recs.add(Recommendation.builder()
                            .learningPath(learningPath)
                            .title("[Demo] Ôn tập kiến thức nền tảng: " + subName)
                            .subjectCode(subCode)
                            .learningObjective("Nắm vững các khái niệm lý thuyết cốt lõi, cú pháp cơ bản và giải được 80% bài tập trắc nghiệm chương 1-3.")
                            .estimatedDuration("10 giờ (2 tuần)")
                            .resourceType("Tài liệu cơ bản & Bài tập mẫu có lời giải (Nội dung Demo)")
                            .explanation(String.format("Môn %s có điểm tổng kết hiện tại là %s/10 (< 5.0). Trình độ AI phân loại là Low. Cần ôn tập lại toàn bộ kiến thức nền tảng ngay để đủ điều kiện thi kết thúc học phần.", subName, score))
                            .priority("HIGH")
                            .status("NOT_STARTED")
                            .description(String.format("Tham gia lớp phụ đạo tăng cường môn %s vào thứ 5 hàng tuần. Đọc lại slide bài giảng từ tuần 1 đến tuần 6.", subName))
                            .build());
                } else if (score.compareTo(BigDecimal.valueOf(7.0)) < 0) {
                    recs.add(Recommendation.builder()
                            .learningPath(learningPath)
                            .title("[Demo] Luyện tập bài tập thực hành cơ bản: " + subName)
                            .subjectCode(subCode)
                            .learningObjective("Thực hành giải bài tập tuần và hoàn thành bài tập nộp LMS đúng hạn.")
                            .estimatedDuration("6 giờ (1 tuần)")
                            .resourceType("Bộ bài tập thực hành cơ bản trên LMS (Nội dung Demo)")
                            .explanation(String.format("Điểm môn %s đạt %s/10 (Trung bình). Cần duy trì làm bài tập thực hành để tránh bị tụt điểm vào cuối kỳ.", subName, score))
                            .priority("MEDIUM")
                            .status("NOT_STARTED")
                            .description(String.format("Hoàn thành các bài lab thực hành môn %s trước ngày chủ nhật.", subName))
                            .build());
                }
            }

            // 2. Thói quen học tập & Hỗ trợ từ cố vấn
            recs.add(Recommendation.builder()
                    .learningPath(learningPath)
                    .title("[Demo] Thiết lập thời khóa biểu tự học tối thiểu 8 giờ/tuần")
                    .subjectCode("GENERAL")
                    .learningObjective("Xây dựng thói quen tự học cố định 1.5 - 2 giờ mỗi ngày tại thư viện hoặc phòng tự học.")
                    .estimatedDuration("Duy trì 6 tuần")
                    .resourceType("Mẫu thời khóa biểu quản lý thời gian Pomodoro (Nội dung Demo)")
                    .explanation("Dữ liệu hoạt động ghi nhận số giờ tự học thấp và tần suất vắng học cao. Kỷ luật tự học là yếu tố quan trọng nhất để cải thiện điểm số.")
                    .priority("HIGH")
                    .status("IN_PROGRESS")
                    .description("Sử dụng kỹ thuật Pomodoro (25 phút tập trung, 5 phút nghỉ) để tránh mệt mỏi và nâng cao hiệu suất tiếp thu bài.")
                    .build());

            recs.add(Recommendation.builder()
                    .learningPath(learningPath)
                    .title("[Demo] Đặt lịch tư vấn cùng Cố vấn học tập (Advisor)")
                    .subjectCode("ADVISOR")
                    .learningObjective("Trao đổi trực tiếp với cố vấn học tập để rà soát tiến độ, khó khăn và xin điều chỉnh kế hoạch học.")
                    .estimatedDuration("1 buổi (45 phút)")
                    .resourceType("Biên bản tư vấn học tập & Kế hoạch cam kết (Nội dung Demo)")
                    .explanation("Cố vấn học tập sẽ đóng vai trò người đồng hành giúp bạn tháo gỡ áp lực và lên chiến lược thi cử hợp lý.")
                    .priority("HIGH")
                    .status("NOT_STARTED")
                    .description("Liên hệ qua email hoặc đăng ký lịch hẹn tại văn phòng khoa để gặp cố vấn trước kỳ thi giữa kỳ.")
                    .build());

        } else if ("Medium".equalsIgnoreCase(level) || "MODERATE".equalsIgnoreCase(level)) {
            title = "Lộ trình Tăng tốc & Nâng cao Điểm số Học phần";
            description = String.format("Sinh viên %s đang có mức học lực Trung bình - Khá (AI Confidence: %.1f%%). Mục tiêu là khắc phục chủ đề còn yếu để vươn lên loại Giỏi.",
                    student.getFullName(),
                    prediction != null && prediction.getConfidence() != null ? prediction.getConfidence().doubleValue() * 100 : 90.0);
            learningGoal = "Nâng điểm trung bình các môn từ 6.5 lên 8.0+, làm quen với các bài tập vận dụng cao và tối ưu hóa phương pháp học.";
            estimatedWeeks = "4 tuần";

            // Phân tích môn học
            for (Grade g : sortedGrades) {
                String subName = g.getSubject().getSubjectName();
                String subCode = g.getSubject().getSubjectCode();
                BigDecimal score = g.getScore();

                if (score.compareTo(BigDecimal.valueOf(6.5)) < 0) {
                    recs.add(Recommendation.builder()
                            .learningPath(learningPath)
                            .title("[Demo] Luyện tập chuyên đề còn yếu: " + subName)
                            .subjectCode(subCode)
                            .learningObjective("Lấp lỗ hổng các dạng bài kiểm tra thường mắc lỗi, rèn luyện kỹ năng phân tích bài toán.")
                            .estimatedDuration("8 giờ (1.5 tuần)")
                            .resourceType("Tuyển tập bài tập theo chuyên đề & Chữa đề kiểm tra mẫu (Nội dung Demo)")
                            .explanation(String.format("Môn %s hiện có điểm là %s/10, dưới mức trung bình của bạn. Bạn có đủ năng lực nhưng cần luyện thêm dạng bài tập vận dụng.", subName, score))
                            .priority("HIGH")
                            .status("NOT_STARTED")
                            .description(String.format("Tập trung ôn tập các chương khó của môn %s và làm lại các bài kiểm tra tuần trước.", subName))
                            .build());
                } else {
                    recs.add(Recommendation.builder()
                            .learningPath(learningPath)
                            .title("[Demo] Bài tập vận dụng thực tế: " + subName)
                            .subjectCode(subCode)
                            .learningObjective("Áp dụng lý thuyết môn học vào đồ án mini hoặc bài toán thực tế quy mô nhỏ.")
                            .estimatedDuration("6 giờ (1 tuần)")
                            .resourceType("Đề bài đồ án mẫu & Hướng dẫn thực hành (Nội dung Demo)")
                            .explanation(String.format("Điểm môn %s đạt %s/10 (Khá). Thực hành vận dụng thực tế sẽ giúp bạn đạt điểm tối đa trong phần bài tập lớn.", subName, score))
                            .priority("MEDIUM")
                            .status("NOT_STARTED")
                            .description(String.format("Thực hiện đồ án nhỏ môn %s kết hợp mã nguồn mở.", subName))
                            .build());
                }
            }

            recs.add(Recommendation.builder()
                    .learningPath(learningPath)
                    .title("[Demo] Tham gia nhóm học tập chuyên đề & Thảo luận nhóm")
                    .subjectCode("GENERAL")
                    .learningObjective("Học hỏi kinh nghiệm giải đề từ bạn bè và nâng cao kỹ năng làm việc nhóm.")
                    .estimatedDuration("2 giờ/tuần")
                    .resourceType("Không gian trao đổi trực tuyến trên Discord / LMS (Nội dung Demo)")
                    .explanation("Học nhóm giúp giải quyết nhanh các bài toán khó và giữ vững động lực học tập suốt học kỳ.")
                    .priority("MEDIUM")
                    .status("IN_PROGRESS")
                    .description("Tham gia các buổi thảo luận giải đề thi cuối tuần cùng nhóm bạn trong lớp.")
                    .build());

        } else { // High
            title = "Lộ trình Phát triển Năng lực & Nghiên cứu Nâng cao";
            description = String.format("Sinh viên %s có kết quả học tập xuất sắc (AI Confidence: %.1f%%). Khuyến khích tiếp cận các thử thách nâng cao và nghiên cứu chuyên sâu.",
                    student.getFullName(),
                    prediction != null && prediction.getConfidence() != null ? prediction.getConfidence().doubleValue() * 100 : 96.0);
            learningGoal = "Phát huy tối đa thế mạnh chuyên môn, thử sức với các bài toán quy mô lớn, nghiên cứu khoa học và chuẩn bị hồ sơ học bổng/việc làm.";
            estimatedWeeks = "4 tuần";

            // Phân tích môn học
            for (Grade g : sortedGrades) {
                String subName = g.getSubject().getSubjectName();
                String subCode = g.getSubject().getSubjectCode();
                BigDecimal score = g.getScore();

                recs.add(Recommendation.builder()
                        .learningPath(learningPath)
                        .title("[Demo] Thử thách chuyên sâu & Đồ án thực tế: " + subName)
                        .subjectCode(subCode)
                        .learningObjective("Ứng dụng kiến thức môn học vào bài toán tối ưu hóa hoặc xây dựng sản phẩm hoàn chỉnh.")
                        .estimatedDuration("12 giờ (2 tuần)")
                        .resourceType("Đề tài nghiên cứu ứng dụng & Tài liệu kỹ thuật nâng cao (Nội dung Demo)")
                        .explanation(String.format("Bạn đạt điểm xuất sắc %s/10 môn %s. Hệ thống khuyến khích bạn thử sức với các bài toán mở rộng để xây dựng portfolio ấn tượng.", score, subName))
                        .priority("MEDIUM")
                        .status("NOT_STARTED")
                        .description(String.format("Phát triển một module mở rộng áp dụng kiến thức môn %s và công bố mã nguồn trên GitHub.", subName))
                        .build());
            }

            recs.add(Recommendation.builder()
                    .learningPath(learningPath)
                    .title("[Demo] Tham gia Đề tài Nghiên cứu Khoa học Sinh viên (NCKH)")
                    .subjectCode("RESEARCH")
                    .learningObjective("Tham gia vào nhóm nghiên cứu tại phòng lab của khoa, tiếp cận phương pháp luận khoa học.")
                    .estimatedDuration("Toàn học kỳ")
                    .resourceType("Danh mục các hướng đề tài NCKH tiềm năng của Khoa (Nội dung Demo)")
                    .explanation("Học lực xuất sắc là nền tảng vững chắc để bạn tham gia NCKH và viết bài báo gửi các hội nghị sinh viên.")
                    .priority("LOW")
                    .status("IN_PROGRESS")
                    .description("Gặp giảng viên bộ môn để đăng ký tham gia nghiên cứu về lĩnh vực Trí tuệ nhân tạo hoặc Hệ thống phân tán.")
                    .build());

            recs.add(Recommendation.builder()
                    .learningPath(learningPath)
                    .title("[Demo] Đảm nhiệm vai trò Hỗ trợ học tập đồng đẳng (Peer Tutor)")
                    .subjectCode("MENTOR")
                    .learningObjective("Hỗ trợ phụ đạo bài tập cho các bạn sinh viên cần giúp đỡ trong lớp.")
                    .estimatedDuration("2 giờ/tuần")
                    .resourceType("Tài liệu hướng dẫn trợ giảng sinh viên (Nội dung Demo)")
                    .explanation("Dạy lại cho người khác là phương pháp học tập đỉnh cao giúp bạn làm chủ hoàn toàn kiến thức đã học.")
                    .priority("LOW")
                    .status("NOT_STARTED")
                    .description("Đăng ký với Văn phòng Khoa để trở thành Tutor hỗ trợ các bạn khóa dưới hoặc bạn cùng lớp.")
                    .build());
        }

        learningPath.setTitle(title);
        learningPath.setDescription(description);
        learningPath.setLearningGoal(learningGoal);
        learningPath.setEstimatedWeeks(estimatedWeeks);
        learningPath.setRecommendations(recs);

        return learningPathRepository.save(learningPath);
    }

    public LearningPathResponse mapToResponse(LearningPath path) {
        List<RecommendationResponse> recResponses = (path.getRecommendations() != null)
                ? path.getRecommendations().stream().map(this::mapToRecResponse).toList()
                : List.of();

        int total = recResponses.size();
        int completed = (int) recResponses.stream()
                .filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()))
                .count();
        int progress = total > 0 ? (int) Math.round(((double) completed / total) * 100) : 0;

        return LearningPathResponse.builder()
                .id(path.getId())
                .studentId(path.getStudent() != null ? path.getStudent().getId() : null)
                .studentName(path.getStudent() != null ? path.getStudent().getFullName() : null)
                .studentCode(path.getStudent() != null ? path.getStudent().getStudentCode() : null)
                .title(path.getTitle())
                .description(path.getDescription())
                .learningGoal(path.getLearningGoal())
                .estimatedWeeks(path.getEstimatedWeeks())
                .status(path.getStatus())
                .progressPercentage(progress)
                .totalRecommendations(total)
                .completedRecommendations(completed)
                .createdAt(path.getCreatedAt())
                .recommendations(recResponses)
                .build();
    }

    private RecommendationResponse mapToRecResponse(Recommendation r) {
        return RecommendationResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .description(r.getDescription())
                .priority(r.getPriority())
                .status(r.getStatus())
                .learningObjective(r.getLearningObjective())
                .estimatedDuration(r.getEstimatedDuration())
                .explanation(r.getExplanation())
                .subjectCode(r.getSubjectCode())
                .resourceType(r.getResourceType())
                .build();
    }
}
