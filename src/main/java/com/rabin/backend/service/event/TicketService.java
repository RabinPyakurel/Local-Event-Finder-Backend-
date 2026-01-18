package com.rabin.backend.service.event;

import com.rabin.backend.dto.response.TicketVerifyResponseDto;
import com.rabin.backend.enums.TicketStatus;
import com.rabin.backend.model.EventEnrollment;
import com.rabin.backend.repository.EventEnrollmentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TicketService {

    private final EventEnrollmentRepository enrollmentRepository;

    public TicketService(EventEnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public TicketVerifyResponseDto verifyTicket(Long organizerId, String ticketCode) {

        log.debug("Ticket verification attempt by organizerId={} ticketCode={}", organizerId, ticketCode);

        EventEnrollment enrollment = enrollmentRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket code"));

        // Ensure the organizer owns this event
        if (!enrollment.getEvent().getCreatedBy().getId().equals(organizerId)) {
            log.warn("Unauthorized verification attempt by organizerId={} ticketCode={}", organizerId, ticketCode);
            throw new IllegalStateException("You are not authorized to verify this ticket");
        }

        // Only ACTIVE tickets can be verified
        if (enrollment.getTicketStatus() != TicketStatus.ACTIVE) {
            log.warn("Ticket verification failed - ticket already used/cancelled ticketCode={}", ticketCode);
            throw new IllegalStateException("Ticket is not active");
        }

        // Mark ticket as used
        enrollment.setTicketStatus(TicketStatus.USED);
        enrollmentRepository.save(enrollment);

        log.info("Ticket verified successfully ticketCode={} for eventId={} by organizerId={}",
                ticketCode, enrollment.getEvent().getId(), organizerId);

        TicketVerifyResponseDto response = new TicketVerifyResponseDto();
        response.setTicketCode(enrollment.getTicketCode());
        response.setEventTitle(enrollment.getEvent().getTitle());
        response.setUserId(enrollment.getUser().getId());
        response.setUserFullName(enrollment.getUser().getFullName());
        response.setTicketStatus(enrollment.getTicketStatus());
        response.setMessage("Ticket verified successfully");

        return response;
    }
}
