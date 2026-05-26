package com.gymtracker.dto.response;

import com.gymtracker.enums.WorkoutCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkoutLogResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long templateId;
    private String workoutName;
    private WorkoutCategory category;
    private LocalDate logDate;
    private Integer durationMinutes;
    private Integer caloriesBurned;
    private Integer sets;
    private Integer reps;
    private Double weightKg;
    private String notes;
    private LocalDateTime createdAt;
}
