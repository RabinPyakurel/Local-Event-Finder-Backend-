package com.rabin.backend.service.event;

import com.rabin.backend.dto.response.InterestedEventResponseDto;
import com.rabin.backend.exception.ResourceNotFoundException;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventInterest;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventEnrollmentRepository;
import com.rabin.backend.repository.EventInterestRepository;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventInterestService {

    private final EventInterestRepository interestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventEnrollmentRepository enrollmentRepository;

    /**
     * Mark an event as interested (add to favorites)
     */
    @Transactional
    public void addInterest(Long userId, Long eventId) {
        log.debug("Adding interest for userId={} eventId={}", userId, eventId);

        // Check if already interested
        if (interestRepository.existsByUser_IdAndEvent_Id(userId, eventId)) {
            throw new IllegalStateException("You are already interested in this event");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        // Organizer can't mark their own event as interested
        if (event.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("You cannot mark your own event as interested");
        }

        EventInterest interest = new EventInterest();
        interest.setUser(user);
        interest.setEvent(event);

        interestRepository.save(interest);
        log.info("User {} marked event {} as interested", userId, eventId);
    }

    /**
     * Remove interest from an event
     */
    @Transactional
    public void removeInterest(Long userId, Long eventId) {
        log.debug("Removing interest for userId={} eventId={}", userId, eventId);

        EventInterest interest = interestRepository.findByUser_IdAndEvent_Id(userId, eventId)
                .orElseThrow(() -> new IllegalStateException("You are not interested in this event"));

        interestRepository.delete(interest);
        log.info("User {} removed interest from event {}", userId, eventId);
    }

    /**
     * Toggle interest (add if not exists, remove if exists)
     */
    @Transactional
    public boolean toggleInterest(Long userId, Long eventId) {
        log.debug("Toggling interest for userId={} eventId={}", userId, eventId);

        if (interestRepository.existsByUser_IdAndEvent_Id(userId, eventId)) {
            removeInterest(userId, eventId);
            return false; // Not interested anymore
        } else {
            addInterest(userId, eventId);
            return true; // Now interested
        }
    }

    /**
     * Check if user is interested in an event
     */
    public boolean isInterested(Long userId, Long eventId) {
        return interestRepository.existsByUser_IdAndEvent_Id(userId, eventId);
    }

    /**
     * Get all events user is interested in
     */
    public List<InterestedEventResponseDto> getUserInterestedEvents(Long userId) {
        log.debug("Getting interested events for userId={}", userId);

        List<EventInterest> interests = interestRepository.findByUser_IdOrderByCreatedAtDesc(userId);

        return interests.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get interest count for an event
     */
    public long getEventInterestCount(Long eventId) {
        return interestRepository.countByEvent_Id(eventId);
    }

    /**
     * Get user's total interested events count
     */
    public long getUserInterestCount(Long userId) {
        return interestRepository.countByUser_Id(userId);
    }

    private InterestedEventResponseDto convertToResponseDto(EventInterest interest) {
        Event event = interest.getEvent();
        User organizer = event.getCreatedBy();

        InterestedEventResponseDto dto = new InterestedEventResponseDto();
        dto.setInterestId(interest.getId());
        dto.setInterestedAt(interest.getCreatedAt());

        // Event info
        dto.setEventId(event.getId());
        dto.setEventTitle(event.getTitle());
        dto.setEventDescription(event.getDescription());
        dto.setEventImageUrl(event.getEventImageUrl());
        dto.setVenue(event.getVenue());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setIsPaid(event.getIsPaid());
        dto.setPrice(event.getPrice());
        dto.setEventStatus(event.getEventStatus().name());

        // Organizer info
        dto.setOrganizerId(organizer.getId());
        dto.setOrganizerName(organizer.getFullName());
        dto.setOrganizerProfileImage(organizer.getProfileImageUrl());

        // Stats
        dto.setInterestCount(interestRepository.countByEvent_Id(event.getId()));
        dto.setEnrollmentCount(enrollmentRepository.countByEvent_Id(event.getId()));

        return dto;
    }
}
