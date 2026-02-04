package com.rabin.backend.service.event;

import com.rabin.backend.dto.request.CreateEventDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.enums.InterestCategory;
import com.rabin.backend.enums.NotificationType;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventTag;
import com.rabin.backend.model.EventTagMap;
import com.rabin.backend.model.User;
import com.rabin.backend.enums.PaymentStatus;
import com.rabin.backend.enums.TicketStatus;
import com.rabin.backend.model.EventEnrollment;
import com.rabin.backend.model.Payment;
import com.rabin.backend.repository.EventEnrollmentRepository;
import com.rabin.backend.repository.EventFeedbackRepository;
import com.rabin.backend.repository.EventInterestRepository;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.EventTagMapRepository;
import com.rabin.backend.repository.EventTagRepository;
import com.rabin.backend.repository.GroupEventMapRepository;
import com.rabin.backend.repository.PaymentRepository;
import com.rabin.backend.repository.ReportRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.service.NotificationService;
import com.rabin.backend.util.EmailUtil;
import com.rabin.backend.util.FileUtil;
import com.rabin.backend.util.Haversine;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.rabin.backend.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventTagRepository eventTagRepository;
    private final EventTagMapRepository eventTagMapRepository;
    private final EventEnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final EventFeedbackRepository feedbackRepository;
    private final EventInterestRepository eventInterestRepository;
    private final GroupEventMapRepository groupEventMapRepository;
    private final ReportRepository reportRepository;
    private final EmailUtil emailUtil;
    private final NotificationService notificationService;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        EventTagRepository eventTagRepository,
                        EventTagMapRepository eventTagMapRepository,
                        EventEnrollmentRepository enrollmentRepository,
                        PaymentRepository paymentRepository,
                        EventFeedbackRepository feedbackRepository,
                        EventInterestRepository eventInterestRepository,
                        GroupEventMapRepository groupEventMapRepository,
                        ReportRepository reportRepository,
                        EmailUtil emailUtil,
                        NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventTagRepository = eventTagRepository;
        this.eventTagMapRepository = eventTagMapRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.paymentRepository = paymentRepository;
        this.feedbackRepository = feedbackRepository;
        this.eventInterestRepository = eventInterestRepository;
        this.groupEventMapRepository = groupEventMapRepository;
        this.reportRepository = reportRepository;
        this.emailUtil = emailUtil;
        this.notificationService = notificationService;
    }

    @Transactional
    public EventResponseDto createEvent(CreateEventDto dto, Long organizerId) {
        log.info("📝 Creating event: title='{}', organizerId={}", dto.getTitle(), organizerId);

        // Validate required fields
        validateBasicFields(dto);

        // Validate and normalize start/end dates
        validateAndNormalizeEventDates(dto);

        // Validate organizer
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> {
                    log.error("❌ Organizer not found with ID: {}", organizerId);
                    return new IllegalArgumentException("Organizer not found");
                });

        // Validate event tags
        validateEventTags(dto.getTags());

        // Handle event image upload
        String eventImageUrl = null;
        if (dto.getEventImage() != null && !dto.getEventImage().isEmpty()) {
            try {
                eventImageUrl = FileUtil.saveFile(dto.getEventImage(), "events");
                log.info("✅ Event image saved: {}", eventImageUrl);
            } catch (Exception e) {
                log.error("❌ Failed to save event image: {}", e.getMessage());
                throw new IllegalArgumentException("Failed to save event image: " + e.getMessage());
            }
        }

        // Create and save event
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setVenue(dto.getVenue());
        event.setEventImageUrl(eventImageUrl);
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setEventStatus(EventStatus.ACTIVE);
        event.setCreatedBy(organizer);
        event.setIsPaid(dto.getIsPaid() != null ? dto.getIsPaid() : false);
        event.setPrice(dto.getPrice() != null ? dto.getPrice() : 0.0);
        event.setAvailableSeats(dto.getAvailableSeats());
        event.setBookedSeats(0);

        Event saved = eventRepository.save(event);
        log.info("Event created with id: {} by organizer: {}", saved.getId(), organizerId);

        // Save tags
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            saveEventTags(saved, dto.getTags());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public EventResponseDto updateEvent(Long eventId, CreateEventDto dto, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check organizer
        if (!event.getCreatedBy().getId().equals(organizerId)) {
            throw new IllegalArgumentException("You are not authorized to update this event");
        }

        // Update basic fields
        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getVenue() != null) event.setVenue(dto.getVenue());
        if (dto.getLatitude() != null) event.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) event.setLongitude(dto.getLongitude());

        // Update dates if provided
        if (dto.getStartDate() != null || dto.getEndDate() != null) {
            LocalDateTime start = dto.getStartDate() != null ? dto.getStartDate() : event.getStartDate();
            LocalDateTime end = dto.getEndDate() != null ? dto.getEndDate() : event.getEndDate();

            CreateEventDto tempDto = new CreateEventDto();
            tempDto.setStartDate(start);
            tempDto.setEndDate(end);

            validateAndNormalizeEventDates(tempDto);

            event.setStartDate(tempDto.getStartDate());
            event.setEndDate(tempDto.getEndDate());
        }

        // Update paid event fields
        if (dto.getIsPaid() != null) {
            long enrolledCount = enrollmentRepository.countByEvent_Id(eventId);

            // Prevent changing from FREE to PAID if users are already enrolled
            if (dto.getIsPaid() && !event.getIsPaid() && enrolledCount > 0) {
                throw new IllegalArgumentException(
                        "Cannot change event to paid. " + enrolledCount + " users are already enrolled. " +
                        "Please create a new paid event instead.");
            }

            // When changing from PAID to FREE, automatically process refunds for all paid users
            if (!dto.getIsPaid() && event.getIsPaid() && enrolledCount > 0) {
                log.info("Event {} changing from PAID to FREE. Processing refunds for {} enrolled users.",
                        eventId, enrolledCount);
                processRefundsForEventTypeChange(event);
            }

            event.setIsPaid(dto.getIsPaid());
        }
        if (dto.getPrice() != null) event.setPrice(dto.getPrice());
        if (dto.getAvailableSeats() != null) event.setAvailableSeats(dto.getAvailableSeats());

        // Update image if provided
        if (dto.getEventImage() != null && !dto.getEventImage().isEmpty()) {
            try {
                String imageUrl = FileUtil.saveFile(dto.getEventImage(), "events");
                event.setEventImageUrl(imageUrl);
                log.debug("Event image updated: {}", imageUrl);
            } catch (Exception e) {
                log.error("Failed to save event image", e);
                throw new IllegalArgumentException("Failed to save event image: " + e.getMessage());
            }
        }

        // Update tags
        if (dto.getTags() != null) {
            eventTagMapRepository.deleteByEvent(event);
            if (!dto.getTags().isEmpty()) saveEventTags(event, dto.getTags());
        }

        Event updated = eventRepository.save(event);
        log.info("Event updated: {}", eventId);

        // Notify all enrolled users about event update
        List<EventEnrollment> enrollments = enrollmentRepository.findByEvent_Id(eventId);
        for (EventEnrollment enrollment : enrollments) {
            notificationService.sendNotification(
                    enrollment.getUser().getId(),
                    NotificationType.EVENT_UPDATED,
                    "Event Updated",
                    "The event '" + event.getTitle() + "' has been updated by the organizer",
                    event.getId(),
                    "EVENT"
            );
        }

        return mapToResponse(updated);
    }

    @Transactional
    public void cancelEvent(Long eventId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getCreatedBy().getId().equals(organizerId)) {
            throw new IllegalArgumentException("You are not authorized to cancel this event");
        }

        // Get all enrollments for this event
        List<EventEnrollment> enrollments = enrollmentRepository.findByEvent_Id(eventId);
        log.info("Cancelling event {} with {} enrollments", eventId, enrollments.size());

        // Process refunds, send notifications, and revoke all enrollments
        for (EventEnrollment enrollment : enrollments) {
            // For paid events, mark payment as REFUNDED
            Double refundAmount = null;
            if (event.getIsPaid()) {
                Payment payment = paymentRepository.findByEnrollment(enrollment).orElse(null);
                if (payment != null && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    payment.setPaymentStatus(PaymentStatus.REFUNDED);
                    payment.setEnrollment(null);
                    paymentRepository.save(payment);
                    refundAmount = payment.getAmount();
                    log.info("Payment {} marked for refund, amount: {}", payment.getId(), refundAmount);
                }
            }

            // Send cancellation email notification
            try {
                emailUtil.sendEventCancellationEmail(
                        enrollment.getUser().getEmail(),
                        enrollment.getUser().getFullName(),
                        event.getTitle(),
                        event.getIsPaid(),
                        refundAmount
                );
            } catch (Exception e) {
                log.error("Failed to send cancellation email to user {}: {}",
                        enrollment.getUser().getId(), e.getMessage());
            }

            // Send in-app notification
            notificationService.sendNotification(
                    enrollment.getUser().getId(),
                    NotificationType.EVENT_CANCELLED,
                    "Event Cancelled",
                    "The event '" + event.getTitle() + "' has been cancelled by the organizer",
                    event.getId(),
                    "EVENT"
            );

            // Delete the enrollment record
            enrollmentRepository.delete(enrollment);
        }

        // Reset booked seats and update event status
        event.setBookedSeats(0);
        event.setEventStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
        log.info("Event cancelled: {}, {} enrollments revoked", eventId, enrollments.size());
    }

    // Get all active events (PUBLIC)
    public List<EventResponseDto> getActiveEvents() {
        return eventRepository.findByEventStatus(EventStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get event by ID (PUBLIC)
    public EventResponseDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        return mapToResponse(event);
    }

    // Get events created by a specific organizer (ORGANIZER - includes all statuses)
    public List<EventResponseDto> getOrganizerEvents(Long organizerId) {
        // Ensure organizer exists
        userRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        return eventRepository.findByCreatedBy_Id(organizerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get public active events by organizer (PUBLIC - only ACTIVE events)
    public List<EventResponseDto> getOrganizerPublicEvents(Long organizerId) {
        // Ensure organizer exists
        userRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return eventRepository.findByCreatedBy_IdAndEventStatus(organizerId, EventStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get events filtered by location (optional lat/lon & radius in km)
    public List<EventResponseDto> getEventsByLocation(Double lat, Double lon, Double radiusKm) {
        List<Event> events = eventRepository.findByEventStatus(EventStatus.ACTIVE);

        if (lat == null || lon == null) {
            return events.stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        double radius = radiusKm != null ? radiusKm : 50.0;

        return events.stream()
                .filter(e -> Haversine.distance(lat, lon, e.getLatitude(), e.getLongitude()) <= radius)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Search events with filters (location, radius, tags, search term) - backward compatible
    public List<EventResponseDto> searchEvents(Double lat, Double lon, Double radiusKm,
                                               List<String> tags, String searchTerm) {
        return searchEvents(lat, lon, radiusKm, tags, searchTerm, null);
    }

    // Search events with filters (location, radius, tags, search term, isPaid)
    public List<EventResponseDto> searchEvents(Double lat, Double lon, Double radiusKm,
                                               List<String> tags, String searchTerm, Boolean isPaid) {
        List<Event> events = eventRepository.findByEventStatus(EventStatus.ACTIVE);

        // Filter by location
        if (lat != null && lon != null) {
            double radius = radiusKm != null ? radiusKm : 50.0;
            events = events.stream()
                    .filter(e -> Haversine.distance(lat, lon, e.getLatitude(), e.getLongitude()) <= radius)
                    .toList();
        }

        // Filter by tags/categories
        if (tags != null && !tags.isEmpty()) {
            events = events.stream()
                    .filter(e -> {
                        List<String> eventTags = eventTagMapRepository.findByEvent(e)
                                .stream()
                                .map(tm -> tm.getEventTag().getTagKey())
                                .toList();
                        return tags.stream().anyMatch(eventTags::contains);
                    })
                    .toList();
        }

        // Filter by paid/free status
        if (isPaid != null) {
            events = events.stream()
                    .filter(e -> e.getIsPaid().equals(isPaid))
                    .toList();
        }

        // Filter by search term
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchLower = searchTerm.toLowerCase();
            events = events.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(searchLower) ||
                            e.getDescription().toLowerCase().contains(searchLower) ||
                            e.getVenue().toLowerCase().contains(searchLower))
                    .toList();
        }

        return events.stream().map(this::mapToResponse).toList();
    }


    /**
     * Get upcoming events (starting in the future, sorted by soonest first)
     */
    public List<EventResponseDto> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByEventStatus(EventStatus.ACTIVE).stream()
                .filter(e -> e.getStartDate() != null && e.getStartDate().isAfter(now))
                .sorted((a, b) -> a.getStartDate().compareTo(b.getStartDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get popular events (sorted by interest count + booked seats, descending)
     */
    public List<EventResponseDto> getPopularEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByEventStatus(EventStatus.ACTIVE).stream()
                .filter(e -> e.getEndDate() == null || e.getEndDate().isAfter(now))
                .sorted((a, b) -> {
                    long scoreA = eventInterestRepository.countByEvent_Id(a.getId())
                            + (a.getBookedSeats() != null ? a.getBookedSeats() : 0);
                    long scoreB = eventInterestRepository.countByEvent_Id(b.getId())
                            + (b.getBookedSeats() != null ? b.getBookedSeats() : 0);
                    return Long.compare(scoreB, scoreA);
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --------------------- Helper Methods ---------------------

    private void validateBasicFields(CreateEventDto dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Event title is required");
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty())
            throw new IllegalArgumentException("Event description is required");
        if (dto.getVenue() == null || dto.getVenue().trim().isEmpty())
            throw new IllegalArgumentException("Event venue is required");

        if (dto.getLatitude() == null || dto.getLongitude() == null)
            throw new IllegalArgumentException("Latitude and Longitude are required");
        if (dto.getLatitude() < -90 || dto.getLatitude() > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        if (dto.getLongitude() < -180 || dto.getLongitude() > 180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180");

        if (dto.getIsPaid() != null && dto.getIsPaid() && (dto.getPrice() == null || dto.getPrice() <= 0))
            throw new IllegalArgumentException("Price must be greater than 0 for paid events");
        if (dto.getAvailableSeats() != null && dto.getAvailableSeats() < 0)
            throw new IllegalArgumentException("Available seats cannot be negative");
    }

    private void validateEventTags(List<String> tags) {
        if (tags == null) return;
        for (String tagName : tags) {
            try {
                InterestCategory.valueOf(tagName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid event tag: " + tagName);
            }
        }
    }

    private void validateAndNormalizeEventDates(CreateEventDto dto) {
        LocalDateTime now = LocalDateTime.now();

        if (dto.getStartDate() == null)
            throw new IllegalArgumentException("Event start date is required");
        if (dto.getStartDate().isBefore(now))
            throw new IllegalArgumentException("Event start date cannot be in the past");

        if (dto.getEndDate() == null) {
            dto.setEndDate(dto.getStartDate().plusHours(23).plusMinutes(59).plusSeconds(59));
        }

        if (dto.getEndDate().isBefore(dto.getStartDate()))
            throw new IllegalArgumentException("Event end date cannot be before start date");

        if (dto.getEndDate().isBefore(now))
            throw new IllegalArgumentException("Event end date cannot be in the past");

        // normalize seconds/nanos
        dto.setStartDate(dto.getStartDate().withSecond(0).withNano(0));
        dto.setEndDate(dto.getEndDate().withSecond(0).withNano(0));
    }

    private void saveEventTags(Event event, List<String> tagNames) {
        for (String tagName : tagNames) {
            try {
                InterestCategory category = InterestCategory.valueOf(tagName);

                EventTag tag = eventTagRepository.findByTagKey(category.name())
                        .orElseGet(() -> {
                            EventTag newTag = new EventTag();
                            newTag.setTagKey(category.name());
                            newTag.setDisplayName(category.getDisplayName());
                            return eventTagRepository.save(newTag);
                        });

                EventTagMap tagMap = new EventTagMap();
                tagMap.setEvent(event);
                tagMap.setEventTag(tag);
                eventTagMapRepository.save(tagMap);

                log.debug("Saved tag {} for event {}", category, event.getId());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid tag category: {}", tagName);
            }
        }
    }
    public boolean isEventOrganizer(Long eventId, Long userId) {
        return eventRepository.existsByIdAndCreatedBy_Id(eventId,userId);
    }

    @Transactional
    public void deleteEvent(Long eventId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getCreatedBy().getId().equals(organizerId)) {
            throw new IllegalArgumentException("You are not authorized to delete this event");
        }

        // Process refunds and send notifications before hard deleting
        List<EventEnrollment> enrollments = enrollmentRepository.findByEvent_Id(eventId);
        for (EventEnrollment enrollment : enrollments) {
            if (event.getIsPaid()) {
                Payment payment = paymentRepository.findByEnrollment(enrollment).orElse(null);
                if (payment != null && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    payment.setPaymentStatus(PaymentStatus.REFUNDED);
                    payment.setEnrollment(null);
                    paymentRepository.save(payment);
                    log.info("Payment {} marked for refund before delete, amount: {}", payment.getId(), payment.getAmount());
                }
            }

            notificationService.sendNotification(
                    enrollment.getUser().getId(),
                    NotificationType.EVENT_CANCELLED,
                    "Event Deleted",
                    "The event '" + event.getTitle() + "' has been deleted by the organizer",
                    event.getId(),
                    "EVENT"
            );
        }

        deleteEventWithCleanup(event);
        log.info("Event {} permanently deleted by organizer {}, {} enrollments revoked", eventId, organizerId, enrollments.size());
    }

    @Transactional
    public void deleteEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Process refunds and send notifications before hard deleting
        List<EventEnrollment> enrollments = enrollmentRepository.findByEvent_Id(eventId);
        for (EventEnrollment enrollment : enrollments) {
            if (event.getIsPaid()) {
                Payment payment = paymentRepository.findByEnrollment(enrollment).orElse(null);
                if (payment != null && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    payment.setPaymentStatus(PaymentStatus.REFUNDED);
                    payment.setEnrollment(null);
                    paymentRepository.save(payment);
                    log.info("Payment {} marked for refund before admin delete, amount: {}", payment.getId(), payment.getAmount());
                }
            }

            notificationService.sendNotification(
                    enrollment.getUser().getId(),
                    NotificationType.EVENT_CANCELLED,
                    "Event Removed",
                    "The event '" + event.getTitle() + "' has been removed by admin",
                    event.getId(),
                    "EVENT"
            );
        }

        deleteEventWithCleanup(event);
        log.info("Event {} permanently deleted by admin, {} enrollments revoked", eventId, enrollments.size());
    }

    private void deleteEventWithCleanup(Event event) {
        Long eventId = event.getId();

        // Delete all related entities before deleting the event
        paymentRepository.deleteByEvent_Id(eventId);
        enrollmentRepository.deleteByEvent_Id(eventId);
        feedbackRepository.deleteByEvent_Id(eventId);
        eventInterestRepository.deleteByEvent_Id(eventId);
        groupEventMapRepository.deleteByEvent(event);
        reportRepository.deleteByEvent_Id(eventId);
        eventTagMapRepository.deleteByEvent(event);

        eventRepository.delete(event);
    }

    /**
     * Process refunds for all paid enrollments when event type changes from PAID to FREE
     */
    private void processRefundsForEventTypeChange(Event event) {
        List<EventEnrollment> enrollments = enrollmentRepository.findByEvent_Id(event.getId());
        int refundCount = 0;

        for (EventEnrollment enrollment : enrollments) {
            // Only process refunds for active tickets with completed payments
            if (enrollment.getTicketStatus() == TicketStatus.ACTIVE) {
                Payment payment = paymentRepository.findByEnrollment(enrollment).orElse(null);

                if (payment != null && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    // Mark payment as refunded
                    payment.setPaymentStatus(PaymentStatus.REFUNDED);
                    paymentRepository.save(payment);
                    refundCount++;

                    log.info("Refund processed for enrollment {}. Amount: {}",
                            enrollment.getId(), payment.getAmount());

                    // Send refund notification email
                    sendEventTypeChangeRefundEmail(enrollment, event, payment.getAmount());
                }
            }
        }

        log.info("Event {} type change: {} refunds processed out of {} enrollments",
                event.getId(), refundCount, enrollments.size());
    }

    /**
     * Send email notification about refund due to event type change
     */
    @Async
    private void sendEventTypeChangeRefundEmail(EventEnrollment enrollment, Event event, Double refundAmount) {
        try {
            String subject = "Refund Processed - " + event.getTitle() + " is now FREE!";
            String body = String.format(
                    "Dear %s,\n\n" +
                    "Great news! The event \"%s\" that you enrolled in has been changed to a FREE event.\n\n" +
                    "As a result, your payment of Rs. %.2f has been processed for refund.\n\n" +
                    "Your ticket remains valid and you are still enrolled for the event.\n\n" +
                    "Event Details:\n" +
                    "- Event: %s\n" +
                    "- Venue: %s\n" +
                    "- Date: %s\n" +
                    "- Ticket Code: %s\n\n" +
                    "The refund will be credited to your original payment method within 5-7 business days.\n\n" +
                    "Thank you for your support!\n\n" +
                    "Best regards,\n" +
                    "Local Event Finder Team",
                    enrollment.getUser().getFullName(),
                    event.getTitle(),
                    refundAmount,
                    event.getTitle(),
                    event.getVenue(),
                    event.getStartDate().toLocalDate().toString(),
                    enrollment.getTicketCode()
            );

            emailUtil.sendEmail(enrollment.getUser().getEmail(), subject, body);
            log.debug("Refund notification email sent to {}", enrollment.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send refund notification email to user {}: {}",
                    enrollment.getUser().getId(), e.getMessage());
        }
    }

    private Long getCurrentUserIdOrNull() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                return userDetails.getId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private EventResponseDto mapToResponse(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setVenue(event.getVenue());
        dto.setEventImageUrl(event.getEventImageUrl());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setOrganizerName(event.getCreatedBy().getFullName());
        dto.setEventStatus(event.getEventStatus().name());

        List<EventTagMap> tagMaps = eventTagMapRepository.findByEvent(event);
        List<String> tags = tagMaps.stream()
                .map(tm -> tm.getEventTag().getTagKey())
                .collect(Collectors.toList());
        dto.setTags(tags);

        dto.setIsPaid(event.getIsPaid());
        dto.setPrice(event.getPrice());
        dto.setAvailableSeats(event.getAvailableSeats());
        dto.setBookedSeats(event.getBookedSeats());
        dto.setOrganizerId(event.getCreatedBy().getId());
        dto.setOrganizerProfileImage(event.getCreatedBy().getProfileImageUrl());

        Long currentUserId = getCurrentUserIdOrNull();
        dto.setIsEventOwner(currentUserId != null && currentUserId.equals(event.getCreatedBy().getId()));

        return dto;
    }
}
