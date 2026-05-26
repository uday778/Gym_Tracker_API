package com.gymtracker.repository;

import com.gymtracker.domain.Subscription;
import com.gymtracker.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByMemberIdAndStatus(Long memberId, SubscriptionStatus status);

    Page<Subscription> findAllByStatus(SubscriptionStatus status, Pageable pageable);

    Page<Subscription> findAllByMemberId(Long memberId, Pageable pageable);

    boolean existsByMemberIdAndStatus(Long memberId, SubscriptionStatus status);

    // Fetch subscriptions past their end date that are still ACTIVE (batch expiry job uses this)
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :today AND s.status = 'ACTIVE'")
    List<Subscription> findExpiredActiveSubscriptions(@Param("today") LocalDate today);

    // Analytics — count active members per tier
    @Query("SELECT s.tier, COUNT(s) FROM Subscription s " +
           "WHERE s.status = 'ACTIVE' GROUP BY s.tier")
    List<Object[]> countActiveMembersByTier();
}
