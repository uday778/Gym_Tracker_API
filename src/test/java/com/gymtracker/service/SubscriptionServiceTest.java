package com.gymtracker.service;

import com.gymtracker.domain.Member;
import com.gymtracker.domain.Subscription;
import com.gymtracker.dto.request.SubscriptionRequest;
import com.gymtracker.dto.response.SubscriptionResponse;
import com.gymtracker.enums.SubscriptionStatus;
import com.gymtracker.enums.SubscriptionTier;
import com.gymtracker.exception.InvalidSubscriptionStateException;
import com.gymtracker.exception.ResourceNotFoundException;
import com.gymtracker.repository.MemberRepository;
import com.gymtracker.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService")
class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Member testMember;
    private Subscription activeSubscription;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .firstName("Uday")
                .lastName("Kumar")
                .build();

        activeSubscription = Subscription.builder()
                .id(10L)
                .member(testMember)
                .tier(SubscriptionTier.PREMIUM)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .amountPaid(new BigDecimal("59.99"))
                .build();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create subscription when member exists and has no active sub")
        void shouldCreateSubscription() {
            SubscriptionRequest request = buildRequest();
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(subscriptionRepository.existsByMemberIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                    .thenReturn(false);
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(activeSubscription);

            SubscriptionResponse response = subscriptionService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.getTier()).isEqualTo(SubscriptionTier.PREMIUM);
            assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            verify(subscriptionRepository).save(any(Subscription.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when member does not exist")
        void shouldThrowWhenMemberNotFound() {
            SubscriptionRequest request = buildRequest();
            when(memberRepository.findById(99L)).thenReturn(Optional.empty());
            request.setMemberId(99L);

            assertThatThrownBy(() -> subscriptionService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Member");

            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidSubscriptionStateException when member already has active sub")
        void shouldThrowWhenAlreadyActive() {
            SubscriptionRequest request = buildRequest();
            when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
            when(subscriptionRepository.existsByMemberIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                    .thenReturn(true);

            assertThatThrownBy(() -> subscriptionService.create(request))
                    .isInstanceOf(InvalidSubscriptionStateException.class)
                    .hasMessageContaining("ACTIVE");

            verify(subscriptionRepository, never()).save(any());
        }
    }

    // ── CANCEL ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("should cancel an ACTIVE subscription")
        void shouldCancelActiveSubscription() {
            when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(activeSubscription));
            when(subscriptionRepository.save(any())).thenReturn(activeSubscription);

            SubscriptionResponse response = subscriptionService.cancel(10L);

            verify(subscriptionRepository).save(argThat(s ->
                    s.getStatus() == SubscriptionStatus.CANCELLED));
        }

        @Test
        @DisplayName("should throw InvalidSubscriptionStateException when already CANCELLED")
        void shouldThrowWhenAlreadyCancelled() {
            activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
            when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(activeSubscription));

            assertThatThrownBy(() -> subscriptionService.cancel(10L))
                    .isInstanceOf(InvalidSubscriptionStateException.class)
                    .hasMessageContaining("CANCELLED");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for unknown ID")
        void shouldThrowWhenNotFound() {
            when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.cancel(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── PAUSE / RESUME ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("pause() and resume()")
    class PauseResume {

        @Test
        @DisplayName("should pause an ACTIVE subscription")
        void shouldPauseActiveSubscription() {
            when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(activeSubscription));
            when(subscriptionRepository.save(any())).thenReturn(activeSubscription);

            subscriptionService.pause(10L);

            verify(subscriptionRepository).save(argThat(s ->
                    s.getStatus() == SubscriptionStatus.PAUSED));
        }

        @Test
        @DisplayName("should throw when pausing a non-ACTIVE subscription")
        void shouldThrowWhenPausingNonActive() {
            activeSubscription.setStatus(SubscriptionStatus.PAUSED);
            when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(activeSubscription));

            assertThatThrownBy(() -> subscriptionService.pause(10L))
                    .isInstanceOf(InvalidSubscriptionStateException.class);
        }

        @Test
        @DisplayName("should resume a PAUSED subscription")
        void shouldResumeSubscription() {
            activeSubscription.setStatus(SubscriptionStatus.PAUSED);
            when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(activeSubscription));
            when(subscriptionRepository.save(any())).thenReturn(activeSubscription);

            subscriptionService.resume(10L);

            verify(subscriptionRepository).save(argThat(s ->
                    s.getStatus() == SubscriptionStatus.ACTIVE));
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private SubscriptionRequest buildRequest() {
        SubscriptionRequest req = new SubscriptionRequest();
        req.setMemberId(1L);
        req.setTier(SubscriptionTier.PREMIUM);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusMonths(1));
        req.setAmountPaid(new BigDecimal("59.99"));
        return req;
    }
}
