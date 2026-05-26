package com.gymtracker.controller;

import com.gymtracker.dto.request.MemberRequest;
import com.gymtracker.dto.response.MemberResponse;
import com.gymtracker.service.MemberService;
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
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Gym member profile management")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new gym member", description = "ADMIN only.")
    @ApiResponse(responseCode = "201", description = "Member created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Linked user not found")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @Operation(summary = "List all active members (paginated)", description = "ADMIN and TRAINER only.")
    public ResponseEntity<Page<MemberResponse>> listActive(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(memberService.listActive(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @Operation(summary = "Search members by name or phone")
    public ResponseEntity<Page<MemberResponse>> search(
            @Parameter(description = "Search query (name or phone)") @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(memberService.search(query, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Get member by ID")
    @ApiResponse(responseCode = "404", description = "Member not found")
    public ResponseEntity<MemberResponse> getById(
            @Parameter(description = "Member ID") @PathVariable Long id) {
        return ResponseEntity.ok(memberService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @Operation(summary = "Update member profile")
    public ResponseEntity<MemberResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a member (soft delete)", description = "ADMIN only.")
    @ApiResponse(responseCode = "204", description = "Member deactivated")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        memberService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
