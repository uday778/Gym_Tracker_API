package com.gymtracker.dto.response;

import com.gymtracker.enums.WorkoutCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkoutTemplateResponse {
    private Long id;
    private String name;
    private String description;
    private WorkoutCategory category;
    private Integer durationMinutes;
    private Integer caloriesBurned;
    private String difficultyLevel;
    private String exercisesJson;
    private boolean active;
    private LocalDateTime createdAt;
}
