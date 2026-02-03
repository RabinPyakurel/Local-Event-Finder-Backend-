package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Event that user has marked as interested")
public class InterestedEventResponseDto {
    @Schema(description = "Interest record ID", example = "1")
    private Long interestId;

    @Schema(description = "Timestamp when user marked interest", example = "2024-05-20T14:30:00")
    private LocalDateTime interestedAt;

    @Schema(description = "Event ID", example = "1")
    private Long eventId;

    @Schema(description = "Event title", example = "Tech Conference 2024")
    private String eventTitle;

    @Schema(description = "Event description", example = "Annual technology conference")
    private String eventDescription;

    @Schema(description = "Event image URL", example = "/uploads/events/event-1.jpg")
    private String eventImageUrl;

    @Schema(description = "Event venue", example = "Kathmandu Convention Center")
    private String venue;

    @Schema(description = "Event start date", example = "2024-06-15T10:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Event end date", example = "2024-06-15T18:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Is this a paid event?", example = "true")
    private Boolean isPaid;

    @Schema(description = "Ticket price in NPR", example = "500.00")
    private Double price;

    @Schema(description = "Event status", example = "ACTIVE")
    private String eventStatus;

    @Schema(description = "Event organizer's user ID", example = "1")
    private Long organizerId;

    @Schema(description = "Event organizer's name", example = "John Doe")
    private String organizerName;

    @Schema(description = "Organizer's profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String organizerProfileImage;

    @Schema(description = "Total interest count for this event", example = "150")
    private Long interestCount;

    @Schema(description = "Total enrollment count for this event", example = "45")
    private Long enrollmentCount;
}
