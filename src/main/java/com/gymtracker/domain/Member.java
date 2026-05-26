package com.gymtracker.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_phone", columnList = "phone")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 60)
    private String firstName;

    @Column(nullable = false, length = 60)
    private String lastName;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(length = 255)
    private String address;

    @Column
    private Double heightCm;

    @Column
    private Double weightKg;

    @Column
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkoutLog> workoutLogs = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
