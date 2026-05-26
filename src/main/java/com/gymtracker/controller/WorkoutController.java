package com.gymtracker.controller;

import com.gymtracker.dto.request.WorkoutLogRequest;
import com.gymtracker.dto.request.WorkoutTemplateRequest;
import com.gymtracker.dto.response.WorkoutLogResponse;
import com.gymtracker.dto.response.WorkoutTemplateResponse;
import com.gymtracker.enums.WorkoutCategory;
import com.gymtracker.service.WorkoutService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
@Tag(name = "Workouts", description = "Workout templates and member workout log management")
public class WorkoutController {

    private final WorkoutService workoutService;

    // ── TEMPLATES (read) ──────────────────────────────────────────────────────

    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('MEMBER', 'TRAINER', 'ADMIN')")
    @Operation(summary = "Get all active workout templates",
               description = "Returns the complete list of active templates. Result is Redis-cached.")
    public ResponseEntity<List<WorkoutTemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(workoutService.getAllActiveTemplates());
    }

    @GetMapping("/templates/category/{category}")
    @PreAuthorize("hasAnyRole('MEMBER', 'TRAINER', 'ADMIN')")
    @Operation(summary = "Get templates by category (paginated)")
    public ResponseEntity<Page<WorkoutTemplateResponse>> getTemplatesByCategory(
            @Parameter(description = "Workout category") @PathVariable WorkoutCategory category,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(workoutService.getTemplatesByCategory(category, pageable));
    }

    @GetMapping("/templates/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'TRAINER', 'ADMIN')")
    @Operation(summary = "Get a workout template by ID")
    @ApiResponse(responseCode = "404", description = "Template not found")
    public ResponseEntity<WorkoutTemplateResponse> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getTemplateById(id));
    }

    // ── TEMPLATES (write) — TRAINER + ADMIN only ──────────────────────────────

    @PostMapping("/templates")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @Operation(summary = "Create a new workout template", description = "TRAINER and ADMIN only.")
    @ApiResponse(responseCode = "201", description = "Template created")
    public ResponseEntity<WorkoutTemplateResponse> createTemplate(
            @Valid @RequestBody WorkoutTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutService.createTemplate(request));
    }

    @PutMapping("/templates/{id}")
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @Operation(summary = "Update a workout template. Evicts the template cache.",
               description = "TRAINER and ADMIN only.")
    public ResponseEntity<WorkoutTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutTemplateRequest request) {
        return ResponseEntity.ok(workoutService.updateTemplate(id, request));
    }

    @DeleteMapping("/templates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a workout template (soft delete)", description = "ADMIN only.")
    @ApiResponse(responseCode = "204", description = "Template deactivated")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable Long id) {
        workoutService.deactivateTemplate(id);
        return ResponseEntity.noContent().build();
    }

    // ── WORKOUT LOGS ──────────────────────────────────────────────────────────

    @PostMapping("/logs")
    @PreAuthorize("hasAnyRole('MEMBER', 'TRAINER', 'ADMIN')")
    @Operation(summary = "Log a completed workout session")
    @ApiResponse(responseCode = "201", description = "Workout logged")
    @ApiResponse(responseCode = "404", description = "Member or template not found")
    public ResponseEntity<WorkoutLogResponse> logWorkout(
            @Valid @RequestBody WorkoutLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutService.logWorkout(request));
    }

    @GetMapping("/logs/member/{memberId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'TRAINER', 'ADMIN')")
    @Operation(summary = "Get all workout logs for a member (paginated, newest first)")
    public ResponseEntity<Page<WorkoutLogResponse>> getMemberLogs(
            @PathVariable Long memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(workoutService.getMemberLogs(memberId, pageable));
    }

    @GetMapping("/logs/member/{memberId}/range")
    @PreAuthorize("hasAnyRole('MEMBER', 'TRAINER', 'ADMIN')")
    @Operation(summary = "Get member workout logs within a date range (paginated)")
    public ResponseEntity<Page<WorkoutLogResponse>> getMemberLogsByDateRange(
            @PathVariable Long memberId,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(workoutService.getMemberLogsByDateRange(memberId, from, to, pageable));
    }

    @GetMapping("/logs/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'TRAINER', 'ADMIN')")
    @Operation(summary = "Get a single workout log by ID")
    public ResponseEntity<WorkoutLogResponse> getLogById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getLogById(id));
    }

    @DeleteMapping("/logs/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN')")
    @Operation(summary = "Delete a workout log")
    @ApiResponse(responseCode = "204", description = "Log deleted")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        workoutService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }
}
