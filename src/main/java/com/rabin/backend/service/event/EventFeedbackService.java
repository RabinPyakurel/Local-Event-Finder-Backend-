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

        EventFeedbackResponseDto response = new EventFeedbackResponseDto();
        response.setEventId(event.getId());
        response.setRating(feedback.getRating());
        response.setComment(feedback.getComment());
        response.setCreatedAt(feedback.getCreatedAt());

        return response;
    }
}
