package com.gymtracker.service;

import com.gymtracker.domain.User;
import com.gymtracker.dto.request.LoginRequest;
import com.gymtracker.dto.request.RegisterRequest;
import com.gymtracker.dto.response.AuthResponse;
import com.gymtracker.exception.UserAlreadyExistsException;
import com.gymtracker.repository.UserRepository;
import com.gymtracker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user id={} role={}", saved.getId(), saved.getRole());

        // Authenticate immediately after registration so caller gets a token
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(saved.getEmail())
                .password(saved.getPassword())
                .authorities(saved.getRole().name())
                .build();

        String token = jwtTokenProvider.generateToken(userDetails);
        return buildAuthResponse(saved, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // AuthenticationManager validates credentials and throws BadCredentialsException on failure
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        log.info("User id={} logged in", user.getId());
        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(expirationMs / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
