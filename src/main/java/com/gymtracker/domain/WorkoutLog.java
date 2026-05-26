package com.gymtracker.domain;

import com.gymtracker.enums.WorkoutCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout_logs", indexes = {
        @Index(name = "idx_log_member_date", columnList = "member_id, log_date"),
        @Index(name = "idx_log_date", columnList = "log_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private WorkoutTemplate template;  // nullable — members can log custom workouts

    @Column(nullable = false, length = 100)
    private String workoutName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkoutCategory category;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column
    private Integer caloriesBurned;

    @Column
    private Integer sets;

    @Column
    private Integer reps;

    @Column
    private Double weightKg;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
