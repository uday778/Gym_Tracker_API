package com.gymtracker.repository;

import com.gymtracker.domain.WorkoutTemplate;
import com.gymtracker.enums.WorkoutCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {

    List<WorkoutTemplate> findAllByActiveTrue();

    Page<WorkoutTemplate> findAllByActiveTrueAndCategory(WorkoutCategory category, Pageable pageable);

    Page<WorkoutTemplate> findAllByActiveTrue(Pageable pageable);
}
