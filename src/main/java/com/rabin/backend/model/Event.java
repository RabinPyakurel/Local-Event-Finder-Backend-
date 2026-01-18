package com.rabin.backend.model;

import com.rabin.backend.enums.EventStatus;
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
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 500)
    private String description;

    private String venue;

    @Column(length = 500)
    private String eventImageUrl;

    private LocalDateTime eventDate;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.ACTIVE;

    // Paid event support
    private Boolean isPaid = false;  // Default to free event
    private Double price = 0.0;  // Price in NPR (Nepali Rupees)
    private Integer availableSeats;  // Null = unlimited
    private Integer bookedSeats = 0;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
