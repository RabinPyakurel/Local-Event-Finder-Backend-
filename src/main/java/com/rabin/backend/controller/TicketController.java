package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.TicketVerifyRequestDto;
import com.rabin.backend.dto.response.TicketVerifyResponseDto;
import com.rabin.backend.service.event.TicketService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tickets", description = "Ticket verification and check-in APIs for organizers")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(summary = "Verify ticket", description = "Verify and check-in a ticket using the ticket code from request body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket verified (check response for validity)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not authorized to verify tickets for this event"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
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

    @Operation(summary = "Scan ticket", description = "Verify and check-in a ticket using the ticket code from URL path. Ideal for QR scan apps.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket scanned (check response for validity)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not authorized to verify tickets for this event"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PostMapping("/scan/{ticketCode}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<TicketVerifyResponseDto>> scanTicket(
            @Parameter(description = "Ticket code from QR scan") @PathVariable String ticketCode
    ) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Ticket scan API called by organizerId={} ticketCode={}", organizerId, ticketCode);

        TicketVerifyResponseDto response = ticketService.verifyTicket(organizerId, ticketCode);
        String message = response.isValid() ? "Ticket verified successfully" : "Ticket verification failed";

        return ResponseEntity.ok(GenericApiResponse.ok(200, message, response));
    }

    @Operation(summary = "Get ticket details", description = "Get ticket details without checking in (preview before confirming)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket details retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view tickets for this event"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{ticketCode}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<TicketVerifyResponseDto>> getTicketDetails(
            @Parameter(description = "Ticket code") @PathVariable String ticketCode
    ) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Get ticket details by organizerId={} ticketCode={}", organizerId, ticketCode);

        TicketVerifyResponseDto response = ticketService.getTicketDetails(organizerId, ticketCode);

        return ResponseEntity.ok(GenericApiResponse.ok(200, "Ticket details retrieved", response));
    }
}
