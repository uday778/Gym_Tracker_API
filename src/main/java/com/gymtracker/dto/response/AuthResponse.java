package com.gymtracker.dto.response;

import com.gymtracker.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String email;
    private String fullName;
    private Role role;
}
