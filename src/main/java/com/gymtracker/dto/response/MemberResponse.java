package com.gymtracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private Double heightCm;
    private Double weightKg;
    private boolean active;
    private LocalDateTime createdAt;
}
