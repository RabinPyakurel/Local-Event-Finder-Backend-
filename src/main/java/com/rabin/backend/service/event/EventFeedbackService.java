package com.rabin.backend.service.event;

import com.rabin.backend.dto.request.EventFeedbackRequestDto;
import com.rabin.backend.dto.response.EventFeedbackResponseDto;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventFeedback;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventEnrollmentRepository;
import com.rabin.backend.repository.EventFeedbackRepository;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventFeedbackService {

    private final EventFeedbackRepository feedbackRepository;
    private final EventEnrollmentRepository enrollmentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventFeedbackService(
            EventFeedbackRepository feedbackRepository,
            EventEnrollmentRepository enrollmentRepository,
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.feedbackRepository = feedbackRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EventFeedbackResponseDto submitFeedback(Long userId, EventFeedbackRequestDto dto) {

        log.debug("Feedback submission userId={} eventId={}", userId, dto.getEventId());

        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        if (!enrollmentRepository.existsByUser_IdAndEvent_Id(userId, dto.getEventId())) {
            throw new IllegalStateException("User not enrolled in this event");
        }

        if (feedbackRepository.existsByEvent_IdAndUser_Id(dto.getEventId(), userId)) {
            throw new IllegalStateException("Feedback already submitted");
        }

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EventFeedback feedback = new EventFeedback();
        feedback.setEvent(event);
        feedback.setUser(user);
        feedback.setRating(dto.getRating());
        feedback.setComment(dto.getComment());

        feedbackRepository.save(feedback);

        log.info("Feedback submitted userId={} eventId={}", userId, dto.getEventId());

        return mapToResponse(feedback, userId);
    }

    @Transactional
    public EventFeedbackResponseDto updateFeedback(Long userId, Long eventId, EventFeedbackRequestDto dto) {
        log.debug("Feedback update userId={} eventId={}", userId, eventId);

        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        EventFeedback feedback = feedbackRepository.findByEvent_IdAndUser_Id(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found for this event"));

        feedback.setRating(dto.getRating());
        feedback.setComment(dto.getComment());
        feedbackRepository.save(feedback);

        log.info("Feedback updated userId={} eventId={}", userId, eventId);

        return mapToResponse(feedback, userId);
    }

    @Transactional
    public void deleteFeedback(Long userId, Long eventId) {
        log.debug("Feedback delete userId={} eventId={}", userId, eventId);

        EventFeedback feedback = feedbackRepository.findByEvent_IdAndUser_Id(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found for this event"));

        feedbackRepository.delete(feedback);

        log.info("Feedback deleted userId={} eventId={}", userId, eventId);
    }

    /**
     * Get all feedbacks for an event with isOwner flag
     */
    public List<EventFeedbackResponseDto> getEventFeedbacks(Long eventId, Long currentUserId) {
        log.debug("Get feedbacks for eventId={} currentUserId={}", eventId, currentUserId);

        if (!eventRepository.existsById(eventId)) {
            throw new IllegalArgumentException("Event not found");
        }

        List<EventFeedback> feedbacks = feedbackRepository.findByEvent_Id(eventId);

        return feedbacks.stream()
                .map(f -> mapToResponse(f, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Get the current user's feedback for a specific event
     */
    public EventFeedbackResponseDto getMyFeedback(Long userId, Long eventId) {
        log.debug("Get my feedback userId={} eventId={}", userId, eventId);

        EventFeedback feedback = feedbackRepository.findByEvent_IdAndUser_Id(eventId, userId)
                .orElse(null);

        if (feedback == null) {
            return null;
        }

        return mapToResponse(feedback, userId);
    }

    private EventFeedbackResponseDto mapToResponse(EventFeedback feedback, Long currentUserId) {
        EventFeedbackResponseDto response = new EventFeedbackResponseDto();
        response.setId(feedback.getId());
        response.setEventId(feedback.getEvent().getId());
        response.setUserId(feedback.getUser().getId());
        response.setUserName(feedback.getUser().getFullName());
        response.setUserProfileImage(feedback.getUser().getProfileImageUrl());
        response.setIsOwner(currentUserId != null && currentUserId.equals(feedback.getUser().getId()));
        response.setRating(feedback.getRating());
        response.setComment(feedback.getComment());
        response.setCreatedAt(feedback.getCreatedAt());
        response.setUpdatedAt(feedback.getUpdatedAt());
        return response;
    }
}
