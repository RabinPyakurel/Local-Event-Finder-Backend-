package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Event details response")
public class EventResponseDto {
    @Schema(description = "Event ID", example = "1")
    private Long id;

    @Schema(description = "Event title", example = "Tech Conference 2024")
    private String title;

    @Schema(description = "Event description", example = "Annual technology conference")
    private String description;

    @Schema(description = "Event venue", example = "Kathmandu Convention Center")
    private String venue;

    @Schema(description = "Event image URL", example = "/uploads/events/event-123.jpg")
    private String eventImageUrl;

    @Schema(description = "Event start date and time", example = "2024-06-15T10:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Event end date and time", example = "2024-06-15T18:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Venue latitude", example = "27.7172")
    private Double latitude;

    @Schema(description = "Venue longitude", example = "85.3240")
    private Double longitude;

    @Schema(description = "Organizer's name", example = "John Doe")
    private String organizerName;

    @Schema(description = "Organizer's profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String organizerProfileImage;

    @Schema(description = "Organizer's user ID", example = "1")
    private Long organizerId;

    @Schema(description = "Event status", example = "ACTIVE")
    private String eventStatus;

    @Schema(description = "List of event tags", example = "[\"TECHNOLOGY\", \"NETWORKING\"]")
    private List<String> tags;

    @Schema(description = "Recommendation score (internal use)", example = "0.85")
    private Double finalScore;

    @Schema(description = "Is this a paid event?", example = "true")
    private Boolean isPaid;

    @Schema(description = "Ticket price in NPR", example = "500.00")
    private Double price;

    @Schema(description = "Total available seats", example = "100")
    private Integer availableSeats;

    @Schema(description = "Number of booked seats", example = "45")
    private Integer bookedSeats;

    @Schema(description = "Number of users interested in this event", example = "150")
    private Long interestCount;

    @Schema(description = "Whether current user is interested (for authenticated requests)", example = "true")
    private Boolean isInterested;

    @Schema(description = "Whether current user is owner (for authenticated requests)", example = "true")
    private Boolean isEventOwner;
}
