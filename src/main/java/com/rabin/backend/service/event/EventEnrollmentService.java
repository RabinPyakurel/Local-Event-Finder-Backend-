package com.rabin.backend.service.event;

import com.rabin.backend.dto.response.EventEnrollmentResponseDto;
import com.rabin.backend.dto.response.EventTicketResponseDto;
import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.enums.NotificationType;
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
import com.rabin.backend.service.NotificationService;
import com.rabin.backend.util.TicketCodeGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final NotificationService notificationService;

    public EventEnrollmentService(
            EventEnrollmentRepository enrollmentRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            ModelMapper modelMapper,
            NotificationService notificationService
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public List<EventTicketResponseDto> enroll(Long userId, Long eventId, Integer numberOfTickets) {

        log.debug("Ticket enrollment userId={} eventId={} tickets={}", userId, eventId, numberOfTickets);

        if (numberOfTickets == null || numberOfTickets < 1) {
            numberOfTickets = 1;
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getEventStatus() != EventStatus.ACTIVE) {
            throw new IllegalStateException("Event not open for enrollment");
        }

        if (event.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("Organizer cannot enroll in own event");
        }

        // Check max tickets per user limit
        long existingTickets = enrollmentRepository.countByUser_IdAndEvent_Id(userId, eventId);
        int maxPerUser = event.getMaxTicketsPerUser() != null ? event.getMaxTicketsPerUser() : 10;

        if (existingTickets + numberOfTickets > maxPerUser) {
            long canBook = maxPerUser - existingTickets;
            if (canBook <= 0) {
                throw new IllegalStateException(
                        String.format("You have reached the maximum limit of %d tickets for this event", maxPerUser));
            }
            throw new IllegalStateException(
                    String.format("You can only book %d more ticket(s). Maximum %d per user.", canBook, maxPerUser));
        }

        // Check seat availability (null = unlimited)
        if (event.getAvailableSeats() != null) {
            int bookedSeats = event.getBookedSeats() != null ? event.getBookedSeats() : 0;
            int remaining = event.getAvailableSeats() - bookedSeats;

            if (remaining < numberOfTickets) {
                if (remaining <= 0) {
                    throw new IllegalStateException("This event is fully booked");
                }
                throw new IllegalStateException(
                        String.format("Only %d seat(s) available. Cannot book %d tickets.", remaining, numberOfTickets));
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // For paid events, verify enough payments exist
        List<Payment> availablePayments = new java.util.ArrayList<>();
        if (event.getIsPaid()) {
            availablePayments = paymentRepository.findByUser(user).stream()
                    .filter(p -> p.getEvent().getId().equals(eventId))
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
                    .filter(p -> p.getEnrollment() == null) // Not already used
                    .collect(Collectors.toList());

            if (availablePayments.size() < numberOfTickets) {
                throw new IllegalStateException(
                        String.format("You have %d completed payment(s) but requested %d ticket(s). Please complete payment first.",
                                availablePayments.size(), numberOfTickets));
            }
        }

        // Create enrollments
        List<EventEnrollment> enrollments = new java.util.ArrayList<>();
        for (int i = 0; i < numberOfTickets; i++) {
            EventEnrollment enrollment = new EventEnrollment();
            enrollment.setUser(user);
            enrollment.setEvent(event);
            enrollment.setTicketCode(TicketCodeGenerator.generate());
            enrollments.add(enrollmentRepository.save(enrollment));

            // Link payment to enrollment if paid event
            if (event.getIsPaid() && i < availablePayments.size()) {
                Payment payment = availablePayments.get(i);
                payment.setEnrollment(enrollment);
                paymentRepository.save(payment);
            }
        }

        // Update booked seats count
        int currentBooked = event.getBookedSeats() != null ? event.getBookedSeats() : 0;
        event.setBookedSeats(currentBooked + numberOfTickets);
        eventRepository.save(event);

        log.info("{} ticket(s) issued for user {} on event {}", numberOfTickets, userId, eventId);

        // Notify event organizer
        notificationService.sendNotification(
                event.getCreatedBy().getId(),
                NotificationType.EVENT_ENROLLMENT,
                "New Enrollment",
                user.getFullName() + " enrolled in your event '" + event.getTitle() + "' (" + numberOfTickets + " ticket(s))",
                event.getId(),
                "EVENT"
        );

        return enrollments.stream()
                .map(e -> buildTicketResponse(e, event))
                .collect(Collectors.toList());
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

        // Mark linked payment as REFUNDED for paid events
        if (event.getIsPaid()) {
            paymentRepository.findByEnrollment(enrollment).ifPresent(payment -> {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setEnrollment(null);
                paymentRepository.save(payment);
                log.info("Payment {} marked as REFUNDED for enrollment {}", payment.getId(), enrollmentId);
            });
        }

        // Decrement booked seats
        if (event.getBookedSeats() != null && event.getBookedSeats() > 0) {
            event.setBookedSeats(event.getBookedSeats() - 1);
            eventRepository.save(event);
        }

        enrollmentRepository.delete(enrollment);
        log.info("Enrollment {} cancelled successfully", enrollmentId);

        // Notify event organizer
        notificationService.sendNotification(
                event.getCreatedBy().getId(),
                NotificationType.EVENT_CANCELLATION,
                "Enrollment Cancelled",
                enrollment.getUser().getFullName() + " cancelled enrollment in your event '" + event.getTitle() + "'",
                event.getId(),
                "EVENT"
        );
    }

    /**
     * Check if user is enrolled in an event
     */
    public boolean isEnrolled(Long userId, Long eventId) {
        return enrollmentRepository.existsByUser_IdAndEvent_Id(userId, eventId);
    }

    /**
     * Get all user's tickets for a specific event
     */
    public List<EventTicketResponseDto> getUserTickets(Long userId, Long eventId) {
        log.debug("Getting tickets for userId={} eventId={}", userId, eventId);

        List<EventEnrollment> enrollments = enrollmentRepository.findByUser_IdAndEvent_Id(userId, eventId);

        if (enrollments.isEmpty()) {
            throw new ResourceNotFoundException("Enrollment not found. You are not enrolled in this event.");
        }

        return enrollments.stream()
                .map(e -> buildTicketResponse(e, e.getEvent()))
                .collect(Collectors.toList());
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
     * Get upcoming enrolled events for a user (events that haven't ended yet)
     */
    public List<EventEnrollmentResponseDto> getUpcomingEnrollments(Long userId) {
        log.debug("Getting upcoming enrollments for userId: {}", userId);

        LocalDateTime now = LocalDateTime.now();
        List<EventEnrollment> enrollments = enrollmentRepository.findByUser_Id(userId);

        return enrollments.stream()
                .filter(e -> e.getEvent().getEndDate() != null && e.getEvent().getEndDate().isAfter(now))
                .filter(e -> e.getEvent().getEventStatus() == EventStatus.ACTIVE)
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get attended event history for a user (events that have already ended)
     */
    public List<EventEnrollmentResponseDto> getAttendedEventHistory(Long userId) {
        log.debug("Getting attended event history for userId: {}", userId);

        LocalDateTime now = LocalDateTime.now();
        List<EventEnrollment> enrollments = enrollmentRepository.findByUser_Id(userId);

        return enrollments.stream()
                .filter(e -> e.getEvent().getEndDate() != null && e.getEvent().getEndDate().isBefore(now))
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert EventEnrollment to EventEnrollmentResponseDto
     */
    private EventEnrollmentResponseDto convertToResponseDto(EventEnrollment enrollment) {
        EventEnrollmentResponseDto dto = modelMapper.map(enrollment, EventEnrollmentResponseDto.class);
        dto.setEnrollmentId(enrollment.getId());
        dto.setEventId(enrollment.getEvent().getId());
        dto.setEventTitle(enrollment.getEvent().getTitle());
        dto.setStartDate(enrollment.getEvent().getStartDate());
        dto.setEndDate(enrollment.getEvent().getEndDate());
        dto.setVenue(enrollment.getEvent().getVenue());
        dto.setEventImageUrl(enrollment.getEvent().getEventImageUrl());
        dto.setEventStatus(enrollment.getEvent().getEventStatus().name());
        dto.setUserId(enrollment.getUser().getId());
        dto.setUserFullName(enrollment.getUser().getFullName());
        dto.setUserEmail(enrollment.getUser().getEmail());
        return dto;
    }

}

