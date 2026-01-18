package com.rabin.backend.dto.response;

import com.rabin.backend.enums.TicketStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventTicketResponseDto {

    private String ticketCode;
    private String eventTitle;
    private String venue;
    private LocalDateTime eventDate;
    private TicketStatus ticketStatus;
    private LocalDateTime enrolledAt;

    private String qrCodeBase64;
}
