package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.EventEnrollmentRequestDto;
import com.rabin.backend.dto.response.EventEnrollmentResponseDto;
import com.rabin.backend.dto.response.EventTicketResponseDto;
import com.rabin.backend.service.event.EventEnrollmentService;
import com.rabin.backend.util.SecurityUtil;
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
public class EventEnrollmentController {

    private final EventEnrollmentService enrollmentService;

    public EventEnrollmentController(EventEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GenericApiResponse<EventTicketResponseDto>> enroll(
            @RequestBody EventEnrollmentRequestDto dto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Enroll API called by userId {}", userId);

        EventTicketResponseDto ticket = enrollmentService.enroll(userId, dto.getEventId());

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Enrolled successfully", ticket)
        );
    }

    /**
     * Get all enrollments for the current user
     */
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

    /**
     * Get all enrollments for a specific event (organizer only)
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<List<EventEnrollmentResponseDto>>> getEventEnrollments(
            @PathVariable Long eventId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get event enrollments for eventId: {} by userId: {}", eventId, userId);

        List<EventEnrollmentResponseDto> enrollments = enrollmentService.getEventEnrollments(eventId, userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event enrollments retrieved successfully", enrollments)
        );
    }

    /**
     * Cancel an enrollment
     */
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Void>> cancelEnrollment(@PathVariable Long enrollmentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Cancel enrollment {} by userId: {}", enrollmentId, userId);

        enrollmentService.cancelEnrollment(enrollmentId, userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Enrollment cancelled successfully", null)
        );
    }

    /**
     * Check if user is enrolled in an event
     */
    @GetMapping("/check/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Boolean>>> checkEnrollment(@PathVariable Long eventId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Check enrollment for userId: {} and eventId: {}", userId, eventId);

        boolean isEnrolled = enrollmentService.isEnrolled(userId, eventId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isEnrolled", isEnrolled);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Enrollment status checked", response)
        );
    }

    /**
     * Get user's ticket for a specific event (with QR code)
     */
    @GetMapping("/ticket/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<EventTicketResponseDto>> getMyTicket(@PathVariable Long eventId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get ticket for userId: {} eventId: {}", userId, eventId);

        EventTicketResponseDto ticket = enrollmentService.getUserTicket(userId, eventId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Ticket retrieved successfully", ticket)
        );
    }

}
