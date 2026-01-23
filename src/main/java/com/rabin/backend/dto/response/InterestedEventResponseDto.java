package com.rabin.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterestedEventResponseDto {
    private Long interestId;
    private LocalDateTime interestedAt;

    // Event info
    private Long eventId;
    private String eventTitle;
    private String eventDescription;
    private String eventImageUrl;
    private String venue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isPaid;
    private Double price;
    private String eventStatus;

    // Organizer info
    private Long organizerId;
    private String organizerName;
    private String organizerProfileImage;

    // Stats
    private Long interestCount;
    private Long enrollmentCount;
}
