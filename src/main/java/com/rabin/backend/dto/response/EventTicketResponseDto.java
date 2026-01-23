package com.rabin.backend.dto.response;

import com.rabin.backend.enums.TicketStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventTicketResponseDto {

    // Ticket info
    private String ticketCode;
    private TicketStatus ticketStatus;
    private LocalDateTime enrolledAt;
    private LocalDateTime checkedInAt;

    // Event info
    private Long eventId;
    private String eventTitle;
    private String eventImageUrl;
    private String venue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Organizer info
    private String organizerName;
}
