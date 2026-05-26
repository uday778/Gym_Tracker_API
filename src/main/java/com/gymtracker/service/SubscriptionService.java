package com.gymtracker.service;

import com.gymtracker.domain.Member;
import com.gymtracker.domain.Subscription;
import com.gymtracker.dto.request.SubscriptionRequest;
import com.gymtracker.dto.response.SubscriptionResponse;
import com.gymtracker.enums.SubscriptionStatus;
import com.gymtracker.exception.InvalidSubscriptionStateException;
import com.gymtracker.exception.ResourceNotFoundException;
import com.gymtracker.repository.MemberRepository;
import com.gymtracker.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public SubscriptionResponse create(SubscriptionRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));

        // Business rule: a member cannot have two concurrent ACTIVE subscriptions
        if (subscriptionRepository.existsByMemberIdAndStatus(request.getMemberId(), SubscriptionStatus.ACTIVE)) {
            throw new InvalidSubscriptionStateException(
                    "Member id=" + request.getMemberId() + " already has an ACTIVE subscription. " +
                    "Cancel or let it expire before creating a new one.");
        }

        Subscription subscription = Subscription.builder()
                .member(member)
                .tier(request.getTier())
                .status(SubscriptionStatus.ACTIVE)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .amountPaid(request.getAmountPaid())
                .notes(request.getNotes())
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Created subscription id={} memberId={} tier={}", saved.getId(), member.getId(), saved.getTier());
        return toResponse(saved);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public SubscriptionResponse getById(Long id) {
        return subscriptionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", id));
    }

    /**
     * Result is cached in Redis per (status, page). Cache is evicted whenever
     * an admin mutates a subscription, keeping reads eventually consistent.
     */
    @Cacheable(value = "subscriptionTiers", key = "#status.name() + '-p' + #pageable.pageNumber")
    public Page<SubscriptionResponse> listByStatus(SubscriptionStatus status, Pageable pageable) {
        return subscriptionRepository.findAllByStatus(status, pageable).map(this::toResponse);
    }

    public Page<SubscriptionResponse> listByMember(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }
        return subscriptionRepository.findAllByMemberId(memberId, pageable).map(this::toResponse);
    }

    // ── MUTATIONS ─────────────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "subscriptionTiers", allEntries = true)
    public SubscriptionResponse cancel(Long id) {
        Subscription sub = getSubscriptionOrThrow(id);

        if (sub.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new InvalidSubscriptionStateException("Subscription id=" + id + " is already CANCELLED.");
        }
        if (sub.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new InvalidSubscriptionStateException("Cannot cancel an EXPIRED subscription.");
        }

        sub.setStatus(SubscriptionStatus.CANCELLED);
        log.info("Cancelled subscription id={}", id);
        return toResponse(subscriptionRepository.save(sub));
    }

    @Transactional
    @CacheEvict(value = "subscriptionTiers", allEntries = true)
    public SubscriptionResponse pause(Long id) {
        Subscription sub = getSubscriptionOrThrow(id);

        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new InvalidSubscriptionStateException(
                    "Only ACTIVE subscriptions can be paused. Current status: " + sub.getStatus());
        }

        sub.setStatus(SubscriptionStatus.PAUSED);
        log.info("Paused subscription id={}", id);
        return toResponse(subscriptionRepository.save(sub));
    }

    @Transactional
    @CacheEvict(value = "subscriptionTiers", allEntries = true)
    public SubscriptionResponse resume(Long id) {
        Subscription sub = getSubscriptionOrThrow(id);

        if (sub.getStatus() != SubscriptionStatus.PAUSED) {
            throw new InvalidSubscriptionStateException(
                    "Only PAUSED subscriptions can be resumed. Current status: " + sub.getStatus());
        }
        if (sub.getEndDate().isBefore(LocalDate.now())) {
            throw new InvalidSubscriptionStateException(
                    "Cannot resume — subscription end date has already passed.");
        }

        sub.setStatus(SubscriptionStatus.ACTIVE);
        log.info("Resumed subscription id={}", id);
        return toResponse(subscriptionRepository.save(sub));
    }

    // ── SCHEDULED EXPIRY JOB ─────────────────────────────────────────────────

    /**
     * Runs daily at midnight. Marks all ACTIVE subscriptions past their end date as EXPIRED.
     * In production this would be a separate batch service or a DB-level event trigger.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    @CacheEvict(value = "subscriptionTiers", allEntries = true)
    public void expireSubscriptions() {
        List<Subscription> expired = subscriptionRepository
                .findExpiredActiveSubscriptions(LocalDate.now());

        expired.forEach(s -> s.setStatus(SubscriptionStatus.EXPIRED));
        subscriptionRepository.saveAll(expired);

        if (!expired.isEmpty()) {
            log.info("Expired {} subscriptions", expired.size());
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private Subscription getSubscriptionOrThrow(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", id));
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return SubscriptionResponse.builder()
                .id(s.getId())
                .memberId(s.getMember().getId())
                .memberName(s.getMember().getFullName())
                .tier(s.getTier())
                .tierDisplayName(s.getTier().getDisplayName())
                .status(s.getStatus())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .amountPaid(s.getAmountPaid())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
