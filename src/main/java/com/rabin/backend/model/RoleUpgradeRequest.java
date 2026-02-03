package com.rabin.backend.model;

import com.rabin.backend.enums.RoleUpgradeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_upgrade_requests")
@Getter
@Setter
public class RoleUpgradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 1000)
    private String reason;  // Why user wants to become organizer

    @Enumerated(EnumType.STRING)
    private RoleUpgradeStatus status = RoleUpgradeStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;  // Admin who reviewed the request

    @Column(length = 500)
    private String adminNote;  // Admin's note for approval/rejection

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
