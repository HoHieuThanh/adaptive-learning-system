package com.capstone.adaptivelearning.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "predicted_level", nullable = false, length = 20)
    private String predictedLevel; // "Low", "Medium", "High"

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "model_name", nullable = false, length = 50)
    private String modelName;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

