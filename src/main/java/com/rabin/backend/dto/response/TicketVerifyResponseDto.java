package com.rabin.backend.dto.response;

import com.rabin.backend.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Ticket verification response with attendee and event details")
public class TicketVerifyResponseDto {
    @Schema(description = "Is the ticket valid?", example = "true")
    private boolean valid;

    @Schema(description = "Verification message", example = "Ticket verified successfully")
    private String message;

    @Schema(description = "Ticket code", example = "TKT-ABC123XYZ")
    private String ticketCode;

    @Schema(description = "Current ticket status", example = "CHECKED_IN")
    private TicketStatus ticketStatus;

    @Schema(description = "Attendee user ID", example = "1")
    private Long userId;

    @Schema(description = "Attendee's full name", example = "John Doe")
    private String userFullName;

    @Schema(description = "Attendee's email", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Attendee's profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String userProfileImage;

    @Schema(description = "Event ID", example = "1")
    private Long eventId;

    @Schema(description = "Event title", example = "Tech Conference 2024")
    private String eventTitle;

    @Schema(description = "Event venue", example = "Kathmandu Convention Center")
    private String venue;

    @Schema(description = "Event start date", example = "2024-06-15T10:00:00")
    private LocalDateTime eventStartDate;

    @Schema(description = "Enrollment timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime enrolledAt;

    @Schema(description = "Check-in timestamp", example = "2024-06-15T09:45:00")
    private LocalDateTime checkedInAt;

    @Schema(description = "Was ticket already checked in before this scan?", example = "false")
    private boolean alreadyCheckedIn;
}
