package com.rabin.backend.dto.response;

import com.rabin.backend.enums.TicketStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TicketVerifyResponseDto {
    private boolean valid;
    private String message;

    // Ticket info
    private String ticketCode;
    private TicketStatus ticketStatus;

    // Attendee info
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userProfileImage;

    // Event info
    private Long eventId;
    private String eventTitle;
    private String venue;
    private LocalDateTime eventStartDate;

    // Timestamps
    private LocalDateTime enrolledAt;
    private LocalDateTime checkedInAt;

    // Was this a new check-in or already checked in before?
    private boolean alreadyCheckedIn;
}
