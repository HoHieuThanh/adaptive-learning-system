package com.capstone.adaptivelearning.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_paths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "learning_goal", columnDefinition = "TEXT")
    private String learningGoal;

    @Column(name = "estimated_weeks", length = 50)
    @Builder.Default
    private String estimatedWeeks = "4 tuần";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "IN_PROGRESS"; // "IN_PROGRESS", "COMPLETED", "ARCHIVED"

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Recommendation> recommendations = new ArrayList<>();
}
