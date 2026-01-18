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
import com.rabin.backend.util.QRCodeGenerator;
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

        // Generate QR code
        String qrCode = QRCodeGenerator.generateQRCodeBase64(enrollment.getTicketCode(), 250, 250);

        EventTicketResponseDto response = new EventTicketResponseDto();
        response.setTicketCode(enrollment.getTicketCode());
        response.setEventTitle(event.getTitle());
        response.setVenue(event.getVenue());
        response.setEventDate(event.getEventDate());
        response.setTicketStatus(enrollment.getTicketStatus());
        response.setEnrolledAt(enrollment.getEnrolledAt());

        response.setQrCodeBase64(qrCode);

        return response;
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
     * Convert EventEnrollment to EventEnrollmentResponseDto
     */
    private EventEnrollmentResponseDto convertToResponseDto(EventEnrollment enrollment) {
        EventEnrollmentResponseDto dto = modelMapper.map(enrollment, EventEnrollmentResponseDto.class);
        dto.setEventId(enrollment.getEvent().getId());
        dto.setEventTitle(enrollment.getEvent().getTitle());
        dto.setEventDate(enrollment.getEvent().getEventDate());
        dto.setVenue(enrollment.getEvent().getVenue());
        dto.setUserId(enrollment.getUser().getId());
        dto.setUserFullName(enrollment.getUser().getFullName());
        dto.setUserEmail(enrollment.getUser().getEmail());
        return dto;
    }

}

