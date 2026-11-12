package com.capstone.adaptivelearning.config;

import com.capstone.adaptivelearning.entity.*;
import com.capstone.adaptivelearning.repository.*;
import com.capstone.adaptivelearning.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final LearningActivityRepository activityRepository;
    private final PredictionRepository predictionRepository;
    private final LearningPathService learningPathService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (studentRepository.count() >= 20) {
            log.info("[SEED/DEMO DATA] Đã có ít nhất 20 sinh viên trong cơ sở dữ liệu, bỏ qua seed data.");
            return;
        }

        log.info("==================================================================");
        log.info("[SEED/DEMO DATA] BẮT ĐẦU NẠP DỮ LIỆU DEMO CHO 20 SINH VIÊN");
        log.info("==================================================================");

        // 1. Seed Users (Student, Advisor, Admin)
        if (userRepository.count() == 0) {
            User studentUser = User.builder()
                    .username("student")
                    .password(passwordEncoder.encode("student123"))
                    .fullName("Nguyễn Văn An (Student Demo)")
                    .role("STUDENT")
                    .build();

            User advisorUser = User.builder()
                    .username("advisor")
                    .password(passwordEncoder.encode("advisor123"))
                    .fullName("TS. Trần Thu Hà (Advisor Demo)")
                    .role("ADVISOR")
                    .build();

            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản Trị Viên Hệ Thống (Admin Demo)")
                    .role("ADMIN")
                    .build();

            userRepository.saveAll(List.of(studentUser, advisorUser, adminUser));
            log.info("[SEED/DEMO DATA] Đã tạo 3 tài khoản đăng nhập demo: student/student123, advisor/advisor123, admin/admin123");
        }

        // 2. Seed Subjects
        Subject cs101 = subjectRepository.findBySubjectCode("CS101")
                .orElseGet(() -> subjectRepository.save(Subject.builder().subjectCode("CS101").subjectName("Nhập môn Lập trình").build()));
        Subject cs201 = subjectRepository.findBySubjectCode("CS201")
                .orElseGet(() -> subjectRepository.save(Subject.builder().subjectCode("CS201").subjectName("Cấu trúc Dữ liệu & Giải thuật").build()));
        Subject cs301 = subjectRepository.findBySubjectCode("CS301")
                .orElseGet(() -> subjectRepository.save(Subject.builder().subjectCode("CS301").subjectName("Hệ Quản trị Cơ sở Dữ liệu").build()));
        Subject ai401 = subjectRepository.findBySubjectCode("AI401")
                .orElseGet(() -> subjectRepository.save(Subject.builder().subjectCode("AI401").subjectName("Học máy Cơ bản (Machine Learning)").build()));

        log.info("[SEED/DEMO DATA] Đã nạp 4 môn học mẫu (CS101, CS201, CS301, AI401)");

        // 3. Danh sách 20 Sinh viên với phân bố đa dạng (High, Medium, At-Risk)
        // Group High (5 SV): SV001, SV005, SV008, SV012, SV017
        // Group Medium (9 SV): SV002, SV004, SV006, SV009, SV011, SV013, SV015, SV018, SV020
        // Group Low/At-Risk (6 SV): SV003, SV007, SV010, SV014, SV016, SV019
        String[][] rawStudents = {
                {"SV001", "Nguyễn Văn An", "an.nv@university.edu.vn", "CNTT-K65", "High", "9.0", "12.0", "98.0", "100.0", "48"},
                {"SV002", "Trần Thị Bình", "binh.tt@university.edu.vn", "CNTT-K65", "Medium", "6.8", "6.5", "90.0", "85.0", "28"},
                {"SV003", "Lê Hoàng Cường", "cuong.lh@university.edu.vn", "CNTT-K65", "Low", "3.8", "2.0", "65.0", "40.0", "8"},
                {"SV004", "Phạm Minh Đức", "duc.pm@university.edu.vn", "HTTT-K65", "Medium", "7.2", "7.0", "92.0", "80.0", "30"},
                {"SV005", "Đỗ Mai Hương", "huong.dm@university.edu.vn", "HTTT-K65", "High", "9.4", "14.5", "100.0", "100.0", "62"},
                {"SV006", "Vũ Quốc Huy", "huy.vq@university.edu.vn", "CNTT-K65", "Medium", "6.5", "5.5", "88.0", "75.0", "22"},
                {"SV007", "Đinh Tuấn Kiệt", "kiet.dt@university.edu.vn", "CNTT-K65", "Low", "4.2", "2.5", "68.0", "45.0", "10"},
                {"SV008", "Bùi Khánh Linh", "linh.bk@university.edu.vn", "HTTT-K65", "High", "8.9", "11.0", "96.0", "95.0", "45"},
                {"SV009", "Trịnh Quang Minh", "minh.tq@university.edu.vn", "CNTT-K65", "Medium", "7.0", "6.0", "91.0", "82.0", "26"},
                {"SV010", "Hà Thị Ngọc", "ngoc.ht@university.edu.vn", "HTTT-K65", "Low", "3.5", "1.5", "60.0", "35.0", "6"},
                {"SV011", "Lý Thanh Phong", "phong.lt@university.edu.vn", "CNTT-K65", "Medium", "6.9", "6.5", "89.0", "80.0", "25"},
                {"SV012", "Dương Thu Quỳnh", "quynh.dt@university.edu.vn", "HTTT-K65", "High", "9.1", "13.0", "99.0", "100.0", "55"},
                {"SV013", "Võ Hùng Sơn", "son.vh@university.edu.vn", "CNTT-K65", "Medium", "7.4", "7.5", "93.0", "88.0", "34"},
                {"SV014", "Mai Văn Thắng", "thang.mv@university.edu.vn", "HTTT-K65", "Low", "4.0", "2.0", "62.0", "40.0", "9"},
                {"SV015", "Đặng Thị Uyên", "uyen.dt@university.edu.vn", "CNTT-K65", "Medium", "6.6", "5.0", "86.0", "78.0", "20"},
                {"SV016", "Ngô Bảo Long", "long.nb@university.edu.vn", "CNTT-K65", "Low", "3.2", "1.0", "55.0", "30.0", "5"},
                {"SV017", "Lương Thu Trang", "trang.lt@university.edu.vn", "HTTT-K65", "High", "9.6", "15.0", "100.0", "100.0", "68"},
                {"SV018", "Phan Gia Phúc", "phuc.pg@university.edu.vn", "CNTT-K65", "Medium", "7.1", "6.8", "90.0", "84.0", "27"},
                {"SV019", "Tạ Hoàng Quân", "quan.th@university.edu.vn", "HTTT-K65", "Low", "4.4", "2.2", "70.0", "48.0", "11"},
                {"SV020", "Chu Thị Thảo", "thao.ct@university.edu.vn", "CNTT-K65", "Medium", "6.7", "6.0", "87.0", "81.0", "24"}
        };

        for (String[] row : rawStudents) {
            String code = row[0];
            String name = row[1];
            String email = row[2];
            String className = row[3];
            String level = row[4];
            double avgScore = Double.parseDouble(row[5]);
            double studyHours = Double.parseDouble(row[6]);
            double attRate = Double.parseDouble(row[7]);
            double assignComp = Double.parseDouble(row[8]);
            int lmsLogins = Integer.parseInt(row[9]);

            Student student = studentRepository.findByStudentCode(code)
                    .orElseGet(() -> studentRepository.save(Student.builder()
                            .studentCode(code)
                            .fullName(name)
                            .email(email)
                            .className(className)
                            .build()));

            // Seed Grades nếu chưa có
            if (gradeRepository.findByStudentId(student.getId()).isEmpty()) {
                double s1Score = Math.min(10.0, Math.max(2.0, avgScore + 0.3));
                double s2Score = Math.min(10.0, Math.max(2.0, avgScore - 0.3));
                gradeRepository.save(Grade.builder().student(student).subject(cs101).score(BigDecimal.valueOf(s1Score)).semester("HK1-2026").build());
                gradeRepository.save(Grade.builder().student(student).subject(cs201).score(BigDecimal.valueOf(s2Score)).semester("HK1-2026").build());
                gradeRepository.save(Grade.builder().student(student).subject(cs301).score(BigDecimal.valueOf(avgScore)).semester("HK1-2026").build());
            }

            // Seed Activity nếu chưa có
            if (activityRepository.findByStudentIdOrderByRecordedAtDesc(student.getId()).isEmpty()) {
                activityRepository.save(LearningActivity.builder()
                        .student(student)
                        .studyHours(BigDecimal.valueOf(studyHours))
                        .attendanceRate(BigDecimal.valueOf(attRate))
                        .assignmentCompletion(BigDecimal.valueOf(assignComp))
                        .lmsLogins(lmsLogins)
                        .recordedAt(LocalDateTime.now())
                        .build());
            }

            // Seed Prediction & Learning Path nếu chưa có
            if (predictionRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()).isEmpty()) {
                double conf = 0.88 + (Math.random() * 0.10);
                Prediction p = predictionRepository.save(Prediction.builder()
                        .student(student)
                        .predictedLevel(level)
                        .confidence(BigDecimal.valueOf(conf).setScale(4, RoundingMode.HALF_UP))
                        .modelName("XGBoost")
                        .build());

                learningPathService.generateAdaptivePath(student, p, gradeRepository.findByStudentId(student.getId()), null);
            }
        }

        log.info("[SEED/DEMO DATA] Đã nạp thành công dữ liệu cho 20 sinh viên (5 High, 9 Medium, 6 At-Risk)!");
        log.info("==================================================================");
    }
}
