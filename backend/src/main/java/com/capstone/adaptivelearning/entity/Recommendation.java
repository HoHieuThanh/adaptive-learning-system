package com.capstone.adaptivelearning.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String priority = "MEDIUM"; // "LOW", "MEDIUM", "HIGH"

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "NOT_STARTED"; // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"

    @Column(name = "learning_objective", columnDefinition = "TEXT")
    private String learningObjective;

    @Column(name = "estimated_duration", length = 50)
    private String estimatedDuration; // Ví dụ: "6 giờ", "2 tuần"

    @Column(columnDefinition = "TEXT")
    private String explanation; // Giải thích lý do hệ thống đưa ra đề xuất này

    @Column(name = "subject_code", length = 50)
    private String subjectCode; // Ví dụ: "CS101", "CS201" hoặc null nếu là kỹ năng chung

    @Column(name = "resource_type", length = 100)
    private String resourceType; // Ví dụ: "Tài liệu cơ bản (Demo)", "Bài tập vận dụng (Demo)", "Đề tài chuyên sâu (Demo)"
}
