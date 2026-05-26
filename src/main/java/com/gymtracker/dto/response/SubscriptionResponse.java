package com.gymtracker.dto.response;

import com.gymtracker.enums.SubscriptionStatus;
import com.gymtracker.enums.SubscriptionTier;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private SubscriptionTier tier;
    private String tierDisplayName;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amountPaid;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
