package com.rabin.backend.dto.response;

import com.rabin.backend.enums.TicketStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketVerifyResponseDto {
    private String ticketCode;
    private String eventTitle;
    private Long userId;
    private String userFullName;
    private TicketStatus ticketStatus;
    private String message;
}
