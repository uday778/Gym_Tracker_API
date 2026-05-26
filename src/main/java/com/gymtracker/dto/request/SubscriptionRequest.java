package com.gymtracker.dto.request;

import com.gymtracker.enums.SubscriptionTier;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionRequest {

    @NotNull(message = "Member ID is required")
    @Positive(message = "Member ID must be positive")
    private Long memberId;

    @NotNull(message = "Subscription tier is required")
    private SubscriptionTier tier;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amountPaid;

    @Size(max = 500)
    private String notes;

    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) return true;
        return endDate.isAfter(startDate);
    }
}
