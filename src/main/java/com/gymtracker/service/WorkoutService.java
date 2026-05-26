package com.gymtracker.service;

import com.gymtracker.domain.Member;
import com.gymtracker.domain.WorkoutLog;
import com.gymtracker.domain.WorkoutTemplate;
import com.gymtracker.dto.request.WorkoutLogRequest;
import com.gymtracker.dto.request.WorkoutTemplateRequest;
import com.gymtracker.dto.response.WorkoutLogResponse;
import com.gymtracker.dto.response.WorkoutTemplateResponse;
import com.gymtracker.enums.WorkoutCategory;
import com.gymtracker.exception.ResourceNotFoundException;
import com.gymtracker.repository.MemberRepository;
import com.gymtracker.repository.WorkoutLogRepository;
import com.gymtracker.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class WorkoutService {

    private final WorkoutTemplateRepository templateRepository;
    private final WorkoutLogRepository logRepository;
    private final MemberRepository memberRepository;

    // ── TEMPLATES ─────────────────────────────────────────────────────────────

    @Cacheable(value = "workoutTemplates", key = "'all'")
    public List<WorkoutTemplateResponse> getAllActiveTemplates() {
        return templateRepository.findAllByActiveTrue()
                .stream().map(this::toTemplateResponse).toList();
    }

    @Cacheable(value = "workoutTemplates", key = "#category.name() + '-p' + #pageable.pageNumber")
    public Page<WorkoutTemplateResponse> getTemplatesByCategory(WorkoutCategory category, Pageable pageable) {
        return templateRepository.findAllByActiveTrueAndCategory(category, pageable)
                .map(this::toTemplateResponse);
    }

    public WorkoutTemplateResponse getTemplateById(Long id) {
        return templateRepository.findById(id)
                .map(this::toTemplateResponse)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutTemplate", id));
    }

    @Transactional
    @CacheEvict(value = "workoutTemplates", allEntries = true)
    public WorkoutTemplateResponse createTemplate(WorkoutTemplateRequest request) {
        WorkoutTemplate template = WorkoutTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .durationMinutes(request.getDurationMinutes())
                .caloriesBurned(request.getCaloriesBurned())
                .difficultyLevel(request.getDifficultyLevel())
                .exercisesJson(request.getExercisesJson())
                .active(true)
                .build();

        WorkoutTemplate saved = templateRepository.save(template);
        log.info("Created workout template id={} name={}", saved.getId(), saved.getName());
        return toTemplateResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "workoutTemplates", allEntries = true)
    public WorkoutTemplateResponse updateTemplate(Long id, WorkoutTemplateRequest request) {
        WorkoutTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutTemplate", id));

        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());
        template.setDurationMinutes(request.getDurationMinutes());
        template.setCaloriesBurned(request.getCaloriesBurned());
        template.setDifficultyLevel(request.getDifficultyLevel());
        template.setExercisesJson(request.getExercisesJson());

        return toTemplateResponse(templateRepository.save(template));
    }

    @Transactional
    @CacheEvict(value = "workoutTemplates", allEntries = true)
    public void deactivateTemplate(Long id) {
        WorkoutTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutTemplate", id));
        template.setActive(false);
        templateRepository.save(template);
        log.info("Deactivated workout template id={}", id);
    }

    // ── WORKOUT LOGS ──────────────────────────────────────────────────────────

    @Transactional
    public WorkoutLogResponse logWorkout(WorkoutLogRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));

        WorkoutTemplate template = null;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException("WorkoutTemplate", request.getTemplateId()));
        }

        WorkoutLog workoutLog = WorkoutLog.builder()
                .member(member)
                .template(template)
                .workoutName(request.getWorkoutName())
                .category(request.getCategory())
                .logDate(request.getLogDate())
                .durationMinutes(request.getDurationMinutes())
                .caloriesBurned(request.getCaloriesBurned())
                .sets(request.getSets())
                .reps(request.getReps())
                .weightKg(request.getWeightKg())
                .notes(request.getNotes())
                .build();

        WorkoutLog saved = logRepository.save(workoutLog);
        log.info("Logged workout id={} for memberId={}", saved.getId(), member.getId());
        return toLogResponse(saved);
    }

    public Page<WorkoutLogResponse> getMemberLogs(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }
        return logRepository.findAllByMemberIdOrderByLogDateDesc(memberId, pageable)
                .map(this::toLogResponse);
    }

    public Page<WorkoutLogResponse> getMemberLogsByDateRange(
            Long memberId, LocalDate from, LocalDate to, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }
        return logRepository.findAllByMemberIdAndLogDateBetween(memberId, from, to, pageable)
                .map(this::toLogResponse);
    }

    public WorkoutLogResponse getLogById(Long id) {
        return logRepository.findById(id)
                .map(this::toLogResponse)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutLog", id));
    }

    @Transactional
    public void deleteLog(Long id) {
        if (!logRepository.existsById(id)) {
            throw new ResourceNotFoundException("WorkoutLog", id);
        }
        logRepository.deleteById(id);
        log.info("Deleted workout log id={}", id);
    }

    // ── MAPPERS ───────────────────────────────────────────────────────────────

    private WorkoutTemplateResponse toTemplateResponse(WorkoutTemplate t) {
        return WorkoutTemplateResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .category(t.getCategory())
                .durationMinutes(t.getDurationMinutes())
                .caloriesBurned(t.getCaloriesBurned())
                .difficultyLevel(t.getDifficultyLevel())
                .exercisesJson(t.getExercisesJson())
                .active(t.isActive())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private WorkoutLogResponse toLogResponse(WorkoutLog l) {
        return WorkoutLogResponse.builder()
                .id(l.getId())
                .memberId(l.getMember().getId())
                .memberName(l.getMember().getFullName())
                .templateId(l.getTemplate() != null ? l.getTemplate().getId() : null)
                .workoutName(l.getWorkoutName())
                .category(l.getCategory())
                .logDate(l.getLogDate())
                .durationMinutes(l.getDurationMinutes())
                .caloriesBurned(l.getCaloriesBurned())
                .sets(l.getSets())
                .reps(l.getReps())
                .weightKg(l.getWeightKg())
                .notes(l.getNotes())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
