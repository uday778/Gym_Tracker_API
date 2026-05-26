package com.gymtracker.service;

import com.gymtracker.domain.Member;
import com.gymtracker.domain.User;
import com.gymtracker.dto.request.MemberRequest;
import com.gymtracker.dto.response.MemberResponse;
import com.gymtracker.exception.ResourceNotFoundException;
import com.gymtracker.repository.MemberRepository;
import com.gymtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new com.gymtracker.exception.ResourceNotFoundException(
                    "Phone number already registered: " + request.getPhone());
        }

        Member member = Member.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .active(true)
                .build();

        Member saved = memberRepository.save(member);
        log.info("Created member id={}", saved.getId());
        return toResponse(saved);
    }

    public MemberResponse getById(Long id) {
        return memberRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }

    public Page<MemberResponse> listActive(Pageable pageable) {
        return memberRepository.findAllByActive(true, pageable).map(this::toResponse);
    }

    public Page<MemberResponse> search(String query, Pageable pageable) {
        return memberRepository.searchMembers(query, pageable).map(this::toResponse);
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));

        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setPhone(request.getPhone());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setGender(request.getGender());
        member.setAddress(request.getAddress());
        member.setHeightCm(request.getHeightCm());
        member.setWeightKg(request.getWeightKg());

        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public void deactivate(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
        member.setActive(false);
        memberRepository.save(member);
        log.info("Deactivated member id={}", id);
    }

    private MemberResponse toResponse(Member m) {
        return MemberResponse.builder()
                .id(m.getId())
                .userId(m.getUser().getId())
                .firstName(m.getFirstName())
                .lastName(m.getLastName())
                .fullName(m.getFullName())
                .email(m.getUser().getEmail())
                .phone(m.getPhone())
                .dateOfBirth(m.getDateOfBirth())
                .gender(m.getGender())
                .address(m.getAddress())
                .heightCm(m.getHeightCm())
                .weightKg(m.getWeightKg())
                .active(m.isActive())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
