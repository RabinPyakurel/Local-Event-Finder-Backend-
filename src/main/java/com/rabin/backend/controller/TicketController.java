package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.TicketVerifyRequestDto;
import com.rabin.backend.dto.response.TicketVerifyResponseDto;
import com.rabin.backend.service.event.TicketService;
import com.rabin.backend.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * Verify and check-in a ticket (via request body)
     */
    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<TicketVerifyResponseDto>> verifyTicket(
            @RequestBody TicketVerifyRequestDto dto
    ) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Ticket verify API called by organizerId={} ticketCode={}", organizerId, dto.getTicketCode());

        TicketVerifyResponseDto response = ticketService.verifyTicket(organizerId, dto.getTicketCode());
        String message = response.isValid() ? "Ticket verified successfully" : "Ticket verification failed";

        return ResponseEntity.ok(GenericApiResponse.ok(200, message, response));
    }

    /**
     * Scan and check-in a ticket (via path variable - easier for QR scan apps)
     */
    @PostMapping("/scan/{ticketCode}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<TicketVerifyResponseDto>> scanTicket(
            @PathVariable String ticketCode
    ) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Ticket scan API called by organizerId={} ticketCode={}", organizerId, ticketCode);

        TicketVerifyResponseDto response = ticketService.verifyTicket(organizerId, ticketCode);
        String message = response.isValid() ? "Ticket verified successfully" : "Ticket verification failed";

        return ResponseEntity.ok(GenericApiResponse.ok(200, message, response));
    }

    /**
     * Get ticket details without checking in (preview before confirming)
     */
    @GetMapping("/{ticketCode}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<TicketVerifyResponseDto>> getTicketDetails(
            @PathVariable String ticketCode
    ) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Get ticket details by organizerId={} ticketCode={}", organizerId, ticketCode);

        TicketVerifyResponseDto response = ticketService.getTicketDetails(organizerId, ticketCode);

        return ResponseEntity.ok(GenericApiResponse.ok(200, "Ticket details retrieved", response));
    }
}

