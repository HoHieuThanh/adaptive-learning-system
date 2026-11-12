package com.capstone.adaptivelearning.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "study_hours", nullable = false, precision = 4, scale = 1)
    private BigDecimal studyHours;

    @Column(name = "attendance_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal attendanceRate;

    @Column(name = "assignment_completion", nullable = false, precision = 5, scale = 2)
    private BigDecimal assignmentCompletion;

    @Column(name = "lms_logins", nullable = false)
    private Integer lmsLogins;

    @Column(name = "recorded_at", nullable = false)
    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();
}

