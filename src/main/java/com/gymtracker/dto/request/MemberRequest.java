package com.gymtracker.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 60)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 60)
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be 10-15 digits")
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 10)
    private String gender;

    @Size(max = 255)
    private String address;

    @DecimalMin(value = "50.0", message = "Height must be at least 50cm")
    @DecimalMax(value = "300.0", message = "Height must be at most 300cm")
    private Double heightCm;

    @DecimalMin(value = "10.0", message = "Weight must be at least 10kg")
    @DecimalMax(value = "500.0", message = "Weight must be at most 500kg")
    private Double weightKg;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
}
