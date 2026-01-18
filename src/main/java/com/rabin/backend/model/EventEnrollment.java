package com.rabin.backend.model;

import com.rabin.backend.enums.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "event_enrollments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "event_id"})
        }
)
@Getter
@Setter
public class EventEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, unique = true)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus = TicketStatus.ACTIVE;

    private LocalDateTime enrolledAt;

    @PrePersist
    void onCreate() {
        enrolledAt = LocalDateTime.now();
    }
}
