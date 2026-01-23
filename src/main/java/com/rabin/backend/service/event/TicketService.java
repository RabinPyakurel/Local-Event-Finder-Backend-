package com.rabin.backend.service.event;

import com.rabin.backend.dto.response.TicketVerifyResponseDto;
import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.enums.TicketStatus;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventEnrollment;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventEnrollmentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class TicketService {

    private final EventEnrollmentRepository enrollmentRepository;

    public TicketService(EventEnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Verify and check-in a ticket by scanning QR code
     * Returns detailed response even for invalid/used tickets (doesn't throw)
     */
    @Transactional
    public TicketVerifyResponseDto verifyTicket(Long organizerId, String ticketCode) {
        log.debug("Ticket verification attempt by organizerId={} ticketCode={}", organizerId, ticketCode);

        TicketVerifyResponseDto response = new TicketVerifyResponseDto();
        response.setTicketCode(ticketCode);

        // Find enrollment
        Optional<EventEnrollment> enrollmentOpt = enrollmentRepository.findByTicketCode(ticketCode);

        if (enrollmentOpt.isEmpty()) {
            response.setValid(false);
            response.setMessage("Invalid ticket code. Ticket not found.");
            return response;
        }

        EventEnrollment enrollment = enrollmentOpt.get();
        Event event = enrollment.getEvent();
        User attendee = enrollment.getUser();

        // Check organizer authorization
        if (!event.getCreatedBy().getId().equals(organizerId)) {
            log.warn("Unauthorized verification attempt by organizerId={} ticketCode={}", organizerId, ticketCode);
            response.setValid(false);
            response.setMessage("You are not authorized to verify tickets for this event.");
            return response;
        }

        // Populate response with details
        populateResponseDetails(response, enrollment, event, attendee);

        // Check if ticket is cancelled
        if (enrollment.getTicketStatus() == TicketStatus.CANCELLED) {
            response.setValid(false);
            response.setMessage("This ticket has been cancelled.");
            return response;
        }

        // Check if event is cancelled
        if (event.getEventStatus() == EventStatus.CANCELLED) {
            response.setValid(false);
            response.setMessage("This event has been cancelled.");
            return response;
        }

        // Check if already checked in
        if (enrollment.getTicketStatus() == TicketStatus.USED) {
            response.setValid(true);
            response.setAlreadyCheckedIn(true);
            response.setMessage("Ticket already checked in at " + enrollment.getCheckedInAt());
            return response;
        }

        // Mark as checked in
        enrollment.setTicketStatus(TicketStatus.USED);
        enrollment.setCheckedInAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        response.setValid(true);
        response.setAlreadyCheckedIn(false);
        response.setTicketStatus(TicketStatus.USED);
        response.setCheckedInAt(enrollment.getCheckedInAt());
        response.setMessage("Check-in successful! Welcome, " + attendee.getFullName());

        log.info("Ticket verified successfully ticketCode={} for eventId={} by organizerId={}",
                ticketCode, event.getId(), organizerId);

        return response;
    }

    /**
     * Get ticket details without checking in (preview before scan)
     */
    public TicketVerifyResponseDto getTicketDetails(Long organizerId, String ticketCode) {
        log.debug("Getting ticket details ticketCode={} by organizerId={}", ticketCode, organizerId);

        TicketVerifyResponseDto response = new TicketVerifyResponseDto();
        response.setTicketCode(ticketCode);

        Optional<EventEnrollment> enrollmentOpt = enrollmentRepository.findByTicketCode(ticketCode);

        if (enrollmentOpt.isEmpty()) {
            response.setValid(false);
            response.setMessage("Invalid ticket code. Ticket not found.");
            return response;
        }

        EventEnrollment enrollment = enrollmentOpt.get();
        Event event = enrollment.getEvent();
        User attendee = enrollment.getUser();

        // Check organizer authorization
        if (!event.getCreatedBy().getId().equals(organizerId)) {
            response.setValid(false);
            response.setMessage("You are not authorized to view tickets for this event.");
            return response;
        }

        // Populate all details
        populateResponseDetails(response, enrollment, event, attendee);
        response.setValid(enrollment.getTicketStatus() != TicketStatus.CANCELLED);
        response.setAlreadyCheckedIn(enrollment.getTicketStatus() == TicketStatus.USED);

        if (enrollment.getTicketStatus() == TicketStatus.CANCELLED) {
            response.setMessage("This ticket has been cancelled.");
        } else if (enrollment.getTicketStatus() == TicketStatus.USED) {
            response.setMessage("Already checked in at " + enrollment.getCheckedInAt());
        } else {
            response.setMessage("Valid ticket - Ready for check-in");
        }

        return response;
    }

    private void populateResponseDetails(TicketVerifyResponseDto response,
                                          EventEnrollment enrollment,
                                          Event event,
                                          User attendee) {
        response.setTicketCode(enrollment.getTicketCode());
        response.setTicketStatus(enrollment.getTicketStatus());
        response.setEventId(event.getId());
        response.setEventTitle(event.getTitle());
        response.setVenue(event.getVenue());
        response.setEventStartDate(event.getStartDate());
        response.setUserId(attendee.getId());
        response.setUserFullName(attendee.getFullName());
        response.setUserEmail(attendee.getEmail());
        response.setUserProfileImage(attendee.getProfileImageUrl());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setCheckedInAt(enrollment.getCheckedInAt());
    }
}
