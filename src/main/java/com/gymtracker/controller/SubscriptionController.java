package com.gymtracker.controller;

import com.gymtracker.dto.request.SubscriptionRequest;
import com.gymtracker.dto.response.SubscriptionResponse;
import com.gymtracker.enums.SubscriptionStatus;
import com.gymtracker.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Gym member subscription lifecycle management")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create a new subscription",
        description = "Creates an ACTIVE subscription for a member. **ADMIN only.** " +
                      "A member cannot have two concurrent ACTIVE subscriptions.")
    @ApiResponse(responseCode = "201", description = "Subscription created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Member not found")
    @ApiResponse(responseCode = "422", description = "Member already has an active subscription")
    public ResponseEntity<SubscriptionResponse> create(
            @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "List subscriptions by status (paginated)",
        description = "Returns a paginated list of subscriptions filtered by status. **ADMIN only.**")
    public ResponseEntity<Page<SubscriptionResponse>> listByStatus(
            @Parameter(description = "Filter by status: ACTIVE | PAUSED | CANCELLED | EXPIRED")
            @RequestParam(defaultValue = "ACTIVE") SubscriptionStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(subscriptionService.listByStatus(status, pageable));
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @Operation(summary = "List all subscriptions for a specific member (paginated)")
    @ApiResponse(responseCode = "404", description = "Member not found")
    public ResponseEntity<Page<SubscriptionResponse>> listByMember(
            @PathVariable Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(subscriptionService.listByMember(memberId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @Operation(summary = "Get subscription by ID")
    @ApiResponse(responseCode = "404", description = "Subscription not found")
    public ResponseEntity<SubscriptionResponse> getById(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getById(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel a subscription", description = "ADMIN only.")
    @ApiResponse(responseCode = "200", description = "Subscription cancelled")
    @ApiResponse(responseCode = "422", description = "Already cancelled or expired")
    public ResponseEntity<SubscriptionResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.cancel(id));
    }

    @PatchMapping("/{id}/pause")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Pause an active subscription", description = "ADMIN only.")
    @ApiResponse(responseCode = "422", description = "Subscription is not ACTIVE")
    public ResponseEntity<SubscriptionResponse> pause(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.pause(id));
    }

    @PatchMapping("/{id}/resume")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resume a paused subscription", description = "ADMIN only.")
    @ApiResponse(responseCode = "422", description = "Subscription is not PAUSED or has expired")
    public ResponseEntity<SubscriptionResponse> resume(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.resume(id));
    }
}
