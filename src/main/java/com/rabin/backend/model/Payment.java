package com.rabin.backend.model;

import com.rabin.backend.enums.PaymentMethod;
import com.rabin.backend.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @OneToOne
    @JoinColumn(name = "enrollment_id")
    private EventEnrollment enrollment;

    private Double amount;  // Amount in NPR

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Payment gateway transaction ID
    private String transactionId;

    // Payment gateway specific data (for verification)
    @Column(length = 1000)
    private String paymentData;

    // Frontend callback URL to redirect after payment verification
    @Column(length = 500)
    private String callbackUrl;

    // Refund tracking
    private Boolean refundProcessed = false;  // Has admin actually processed the refund?
    private LocalDateTime refundedAt;         // When refund was processed
    private String refundNote;                // Admin notes about the refund

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
