package com.gymtracker.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-identity rate limiter using Bucket4j token-bucket algorithm.
 *
 * Identity resolution priority:
 *   1. First 40 chars of the JWT token (authenticated users get their own bucket)
 *   2. Client IP address (unauthenticated callers share an IP bucket)
 *
 * This prevents a single abusive caller from consuming shared quota.
 * For production at scale, migrate bucket state to Redis using bucket4j-redis
 * so limits survive pod restarts and work across multiple instances.
 */
@Component
@Order(1)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    // ConcurrentHashMap is safe for concurrent bucket creation; Bucket itself is thread-safe
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        // Skip rate limiting for health checks and Swagger
        if (isExcluded(request)) {
            chain.doFilter(request, response);
            return;
        }

        String key = resolveClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> buildBucket());

        if (bucket.tryConsume(1)) {
            // Add rate-limit info headers so clients can self-throttle
            response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.addHeader("X-RateLimit-Remaining",
                    String.valueOf(bucket.getAvailableTokens()));
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for key={}", key);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("Retry-After", "60");
            response.getWriter().write("""
                    {"timestamp":"%s","status":429,"errorCode":"RATE_LIMIT_EXCEEDED",\
                    "message":"Too many requests. Max %d requests/minute. Retry after 60 seconds."}
                    """.formatted(Instant.now(), requestsPerMinute));
        }
    }

    /**
     * Authenticated callers use the first 40 chars of their token as a stable identity key.
     * This ensures a user's limit is enforced consistently even across IP changes (mobile users).
     */
    private String resolveClientKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 47) {
            return "user:" + authHeader.substring(7, 47);
        }
        // Fall back to IP — use X-Forwarded-For if behind a proxy/load balancer
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private Bucket buildBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(requestsPerMinute)
                        .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private boolean isExcluded(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator/health")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/api-docs");
    }
}
