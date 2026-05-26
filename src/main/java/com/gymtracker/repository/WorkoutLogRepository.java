package com.gymtracker.repository;

import com.gymtracker.domain.WorkoutLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    Page<WorkoutLog> findAllByMemberIdOrderByLogDateDesc(Long memberId, Pageable pageable);

    Page<WorkoutLog> findAllByMemberIdAndLogDateBetween(
            Long memberId, LocalDate from, LocalDate to, Pageable pageable);

    @Query("SELECT SUM(w.caloriesBurned) FROM WorkoutLog w " +
           "WHERE w.member.id = :memberId AND w.logDate BETWEEN :from AND :to")
    Integer sumCaloriesBurnedByMemberAndDateRange(
            @Param("memberId") Long memberId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT COUNT(w) FROM WorkoutLog w " +
           "WHERE w.member.id = :memberId AND w.logDate >= :since")
    long countWorkoutsForMemberSince(
            @Param("memberId") Long memberId,
            @Param("since") LocalDate since);
}
