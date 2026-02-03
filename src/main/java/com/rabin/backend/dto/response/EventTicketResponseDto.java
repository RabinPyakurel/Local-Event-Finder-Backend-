package com.rabin.backend.dto.response;

import com.rabin.backend.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Event ticket details for user's enrolled events")
public class EventTicketResponseDto {

    @Schema(description = "Unique ticket code for QR", example = "TKT-ABC123XYZ")
    private String ticketCode;

    @Schema(description = "Current ticket status", example = "VALID")
    private TicketStatus ticketStatus;

    @Schema(description = "Enrollment timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime enrolledAt;

    @Schema(description = "Check-in timestamp (if checked in)", example = "2024-06-15T09:45:00")
    private LocalDateTime checkedInAt;

    @Schema(description = "Event ID", example = "1")
    private Long eventId;

    @Schema(description = "Event title", example = "Tech Conference 2024")
    private String eventTitle;

    @Schema(description = "Event image URL", example = "/uploads/events/event-1.jpg")
    private String eventImageUrl;

    @Schema(description = "Event venue", example = "Kathmandu Convention Center")
    private String venue;

    @Schema(description = "Event start date", example = "2024-06-15T10:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Event end date", example = "2024-06-15T18:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Event organizer's name", example = "John Doe")
    private String organizerName;
}
