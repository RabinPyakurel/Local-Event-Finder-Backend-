package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.TicketVerifyRequestDto;
import com.rabin.backend.dto.response.TicketVerifyResponseDto;
import com.rabin.backend.service.event.TicketService;
import com.rabin.backend.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<GenericApiResponse<TicketVerifyResponseDto>> verifyTicket(
            @RequestBody TicketVerifyRequestDto dto
    ) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Ticket verify API called by organizerId={} ticketCode={}", organizerId, dto.getTicketCode());

        TicketVerifyResponseDto response = ticketService.verifyTicket(organizerId, dto.getTicketCode());
        return ResponseEntity.ok(GenericApiResponse.ok(200, "Ticket verification successful", response));
    }
}

