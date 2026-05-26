package com.gymtracker.dto.request;

import com.gymtracker.enums.WorkoutCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class WorkoutTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Category is required")
    private WorkoutCategory category;

    @NotNull(message = "Duration is required")
    @Min(5) @Max(300)
    private Integer durationMinutes;

    @NotNull(message = "Calories burned estimate is required")
    @Min(10) @Max(3000)
    private Integer caloriesBurned;

    @NotBlank(message = "Difficulty level is required")
    @Pattern(regexp = "BEGINNER|INTERMEDIATE|ADVANCED",
             message = "Difficulty must be BEGINNER, INTERMEDIATE, or ADVANCED")
    private String difficultyLevel;

    private String exercisesJson;
}
