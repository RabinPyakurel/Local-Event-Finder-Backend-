package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.EventEnrollmentRequestDto;
import com.rabin.backend.dto.response.EventEnrollmentResponseDto;
import com.rabin.backend.dto.response.EventTicketResponseDto;
import com.rabin.backend.service.event.EventEnrollmentService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@Slf4j
@Tag(name = "Event Enrollments", description = "APIs for enrolling in events and managing tickets")
public class EventEnrollmentController {

    private final EventEnrollmentService enrollmentService;

    public EventEnrollmentController(EventEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @Operation(summary = "Enroll in event", description = "Enroll in a free event and get tickets. For paid events, use the payment flow instead.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrolled successfully"),
            @ApiResponse(responseCode = "400", description = "Event is full or paid event requires payment"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER')")
    public ResponseEntity<GenericApiResponse<List<EventTicketResponseDto>>> enroll(
            @RequestBody EventEnrollmentRequestDto dto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Enroll API called by userId {} for {} tickets", userId, dto.getNumberOfTickets());

        List<EventTicketResponseDto> tickets = enrollmentService.enroll(userId, dto.getEventId(), dto.getNumberOfTickets());

        String message = tickets.size() == 1 ? "Enrolled successfully" :
                String.format("%d tickets booked successfully", tickets.size());

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, message, tickets)
        );
    }

    @Operation(summary = "Get my enrollments", description = "Get all event enrollments for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<List<EventEnrollmentResponseDto>>> getUserEnrollments() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get user enrollments for userId: {}", userId);

        List<EventEnrollmentResponseDto> enrollments = enrollmentService.getUserEnrollments(userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Enrollments retrieved successfully", enrollments)
        );
    }

    @Operation(summary = "Get event enrollments", description = "Get all enrollments for a specific event. Only available to event organizer or admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this event's enrollments"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<List<EventEnrollmentResponseDto>>> getEventEnrollments(
            @Parameter(description = "Event ID") @PathVariable Long eventId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get event enrollments for eventId: {} by userId: {}", eventId, userId);

        List<EventEnrollmentResponseDto> enrollments = enrollmentService.getEventEnrollments(eventId, userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event enrollments retrieved successfully", enrollments)
        );
    }

    @Operation(summary = "Cancel enrollment", description = "Cancel an enrollment. Refund will be processed for paid events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollment cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not authorized to cancel this enrollment"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Void>> cancelEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable Long enrollmentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Cancel enrollment {} by userId: {}", enrollmentId, userId);

        enrollmentService.cancelEnrollment(enrollmentId, userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Enrollment cancelled successfully", null)
        );
    }

    @Operation(summary = "Check enrollment status", description = "Check if the current user is enrolled in an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollment status checked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/check/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Boolean>>> checkEnrollment(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Check enrollment for userId: {} and eventId: {}", userId, eventId);

        boolean isEnrolled = enrollmentService.isEnrolled(userId, eventId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isEnrolled", isEnrolled);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Enrollment status checked", response)
        );
    }

    @Operation(summary = "Get my tickets", description = "Get all tickets for the current user for a specific event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/ticket/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<List<EventTicketResponseDto>>> getMyTickets(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get tickets for userId: {} eventId: {}", userId, eventId);

        List<EventTicketResponseDto> tickets = enrollmentService.getUserTickets(userId, eventId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Tickets retrieved successfully", tickets)
        );
    }

    @Operation(summary = "Get upcoming events", description = "Get all upcoming enrolled events for the current user (events that haven't ended yet)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upcoming events retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<List<EventEnrollmentResponseDto>>> getUpcomingEnrollments() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get upcoming enrollments for userId: {}", userId);

        List<EventEnrollmentResponseDto> enrollments = enrollmentService.getUpcomingEnrollments(userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Upcoming events retrieved successfully", enrollments)
        );
    }

    @Operation(summary = "Get attended event history", description = "Get all past events the current user attended (events that have already ended)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<List<EventEnrollmentResponseDto>>> getAttendedEventHistory() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get attended event history for userId: {}", userId);

        List<EventEnrollmentResponseDto> enrollments = enrollmentService.getAttendedEventHistory(userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event history retrieved successfully", enrollments)
        );
    }
}
