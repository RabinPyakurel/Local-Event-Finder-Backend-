package com.rabin.backend.service.event;

import com.rabin.backend.dto.response.EventEnrollmentResponseDto;
import com.rabin.backend.dto.response.EventTicketResponseDto;
import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.enums.PaymentStatus;
import com.rabin.backend.exception.ResourceNotFoundException;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventEnrollment;
import com.rabin.backend.model.Payment;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventEnrollmentRepository;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.PaymentRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.util.TicketCodeGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventEnrollmentService {

    private final EventEnrollmentRepository enrollmentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    public EventEnrollmentService(
            EventEnrollmentRepository enrollmentRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            ModelMapper modelMapper
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public EventTicketResponseDto enroll(Long userId, Long eventId) {

        log.debug("Ticket enrollment userId={} eventId={}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getEventStatus() != EventStatus.ACTIVE) {
            throw new IllegalStateException("Event not open for enrollment");
        }

        if (event.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("Organizer cannot enroll in own event");
        }

        if (enrollmentRepository.existsByUser_IdAndEvent_Id(userId, eventId)) {
            throw new IllegalStateException("Already enrolled");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // For paid events, verify payment
        Payment payment = null;
        if (event.getIsPaid()) {
            // Find completed payment for this user and event
            payment = paymentRepository.findByUser(user).stream()
                    .filter(p -> p.getEvent().getId().equals(eventId))
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
                    .filter(p -> p.getEnrollment() == null) // Not already used
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Payment required for this event. Please complete payment first."));
        }

        EventEnrollment enrollment = new EventEnrollment();
        enrollment.setUser(user);
        enrollment.setEvent(event);
        enrollment.setTicketCode(TicketCodeGenerator.generate());

        enrollmentRepository.save(enrollment);

        // Link payment to enrollment if paid event
        if (payment != null) {
            payment.setEnrollment(enrollment);
            paymentRepository.save(payment);
        }

        log.info("Ticket issued {} for user {}", enrollment.getTicketCode(), userId);

        return buildTicketResponse(enrollment, event);
    }

    /**
     * Get all enrollments for a user
     */
    public List<EventEnrollmentResponseDto> getUserEnrollments(Long userId) {
        log.debug("Getting enrollments for userId: {}", userId);

        List<EventEnrollment> enrollments = enrollmentRepository.findByUser_Id(userId);

        return enrollments.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all enrollments for an event (organizer only)
     */
    public List<EventEnrollmentResponseDto> getEventEnrollments(Long eventId, Long requesterId) {
        log.debug("Getting enrollments for eventId: {} by userId: {}", eventId, requesterId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        // Check if requester is the event organizer or admin
        if (!event.getCreatedBy().getId().equals(requesterId)) {
            throw new IllegalStateException("Only event organizer can view enrollments");
        }

        List<EventEnrollment> enrollments = enrollmentRepository.findByEvent_Id(eventId);

        return enrollments.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Cancel an enrollment
     */
    @Transactional
    public void cancelEnrollment(Long enrollmentId, Long userId) {
        log.debug("Cancelling enrollment {} by userId: {}", enrollmentId, userId);

        EventEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));

        // Check if the user owns this enrollment
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You can only cancel your own enrollments");
        }

        // Check if event has already passed
        Event event = enrollment.getEvent();
        if (event.getEventStatus() == EventStatus.COMPLETED ||
            event.getEventStatus() == EventStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel enrollment for completed or cancelled events");
        }

        enrollmentRepository.delete(enrollment);
        log.info("Enrollment {} cancelled successfully", enrollmentId);
    }

    /**
     * Check if user is enrolled in an event
     */
    public boolean isEnrolled(Long userId, Long eventId) {
        return enrollmentRepository.existsByUser_IdAndEvent_Id(userId, eventId);
    }

    /**
     * Get user's ticket for a specific event
     */
    public EventTicketResponseDto getUserTicket(Long userId, Long eventId) {
        log.debug("Getting ticket for userId={} eventId={}", userId, eventId);

        EventEnrollment enrollment = enrollmentRepository.findByUser_IdAndEvent_Id(userId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found. You are not enrolled in this event."));

        return buildTicketResponse(enrollment, enrollment.getEvent());
    }

    /**
     * Build EventTicketResponseDto from enrollment
     * Note: QR code is generated by frontend using ticketCode
     */
    private EventTicketResponseDto buildTicketResponse(EventEnrollment enrollment, Event event) {
        EventTicketResponseDto response = new EventTicketResponseDto();

        // Ticket info (frontend generates QR from ticketCode)
        response.setTicketCode(enrollment.getTicketCode());
        response.setTicketStatus(enrollment.getTicketStatus());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setCheckedInAt(enrollment.getCheckedInAt());

        // Event info
        response.setEventId(event.getId());
        response.setEventTitle(event.getTitle());
        response.setEventImageUrl(event.getEventImageUrl());
        response.setVenue(event.getVenue());
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());

        // Organizer info
        response.setOrganizerName(event.getCreatedBy().getFullName());

        return response;
    }

    /**
     * Convert EventEnrollment to EventEnrollmentResponseDto
     */
    private EventEnrollmentResponseDto convertToResponseDto(EventEnrollment enrollment) {
        EventEnrollmentResponseDto dto = modelMapper.map(enrollment, EventEnrollmentResponseDto.class);
        dto.setEventId(enrollment.getEvent().getId());
        dto.setEventTitle(enrollment.getEvent().getTitle());
        dto.setStartDate(enrollment.getEvent().getStartDate());
        dto.setVenue(enrollment.getEvent().getVenue());
        dto.setUserId(enrollment.getUser().getId());
        dto.setUserFullName(enrollment.getUser().getFullName());
        dto.setUserEmail(enrollment.getUser().getEmail());
        return dto;
    }

}

