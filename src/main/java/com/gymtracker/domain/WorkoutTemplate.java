package com.gymtracker.domain;

import com.gymtracker.enums.WorkoutCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workout_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkoutCategory category;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer caloriesBurned;

    @Column(nullable = false)
    private String difficultyLevel;  // BEGINNER, INTERMEDIATE, ADVANCED

    @Column(columnDefinition = "TEXT")
    private String exercisesJson;    // JSON array of exercises stored as text

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
