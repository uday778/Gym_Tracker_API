package com.gymtracker.dto.request;

import com.gymtracker.enums.WorkoutCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkoutLogRequest {

    @NotNull(message = "Member ID is required")
    @Positive
    private Long memberId;

    // Optional — member can reference a template or log a custom workout
    private Long templateId;

    @NotBlank(message = "Workout name is required")
    @Size(max = 100)
    private String workoutName;

    @NotNull(message = "Category is required")
    private WorkoutCategory category;

    @NotNull(message = "Log date is required")
    @PastOrPresent(message = "Log date cannot be in the future")
    private LocalDate logDate;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration cannot exceed 600 minutes")
    private Integer durationMinutes;

    @Min(value = 0)
    @Max(value = 5000)
    private Integer caloriesBurned;

    @Min(value = 1) @Max(value = 100)
    private Integer sets;

    @Min(value = 1) @Max(value = 1000)
    private Integer reps;

    @DecimalMin("0.0") @DecimalMax("1000.0")
    private Double weightKg;

    @Size(max = 500)
    private String notes;
}
