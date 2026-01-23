package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.InterestedEventResponseDto;
import com.rabin.backend.service.event.EventInterestService;
import com.rabin.backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventInterestController {

    private final EventInterestService interestService;

    /**
     * Mark an event as interested (click heart)
     */
    @PostMapping("/{eventId}/interest")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> addInterest(
            @PathVariable Long eventId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Add interest for eventId={} by userId={}", eventId, userId);

        interestService.addInterest(userId, eventId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event marked as interested", Map.of(
                        "isInterested", true,
                        "interestCount", interestService.getEventInterestCount(eventId)
                ))
        );
    }

    /**
     * Remove interest from an event (unclick heart)
     */
    @DeleteMapping("/{eventId}/interest")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> removeInterest(
            @PathVariable Long eventId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Remove interest for eventId={} by userId={}", eventId, userId);

        interestService.removeInterest(userId, eventId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Interest removed from event", Map.of(
                        "isInterested", false,
                        "interestCount", interestService.getEventInterestCount(eventId)
                ))
        );
    }

    /**
     * Toggle interest (add if not exists, remove if exists)
     */
    @PostMapping("/{eventId}/interest/toggle")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> toggleInterest(
            @PathVariable Long eventId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Toggle interest for eventId={} by userId={}", eventId, userId);

        boolean isInterested = interestService.toggleInterest(userId, eventId);
        String message = isInterested ? "Event marked as interested" : "Interest removed from event";

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, message, Map.of(
                        "isInterested", isInterested,
                        "interestCount", interestService.getEventInterestCount(eventId)
                ))
        );
    }

    /**
     * Check if user is interested in an event
     */
    @GetMapping("/{eventId}/interest/check")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> checkInterest(
            @PathVariable Long eventId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Check interest for eventId={} by userId={}", eventId, userId);

        boolean isInterested = interestService.isInterested(userId, eventId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Interest status checked", Map.of(
                        "isInterested", isInterested,
                        "interestCount", interestService.getEventInterestCount(eventId)
                ))
        );
    }

    /**
     * Get event interest count (public)
     */
    @GetMapping("/{eventId}/interest/count")
    public ResponseEntity<GenericApiResponse<Map<String, Long>>> getInterestCount(
            @PathVariable Long eventId
    ) {
        log.debug("Get interest count for eventId={}", eventId);

        long count = interestService.getEventInterestCount(eventId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Interest count retrieved", Map.of(
                        "interestCount", count
                ))
        );
    }

    /**
     * Get all events user is interested in (for profile page)
     */
    @GetMapping("/interested")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<List<InterestedEventResponseDto>>> getUserInterestedEvents() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get interested events for userId={}", userId);

        List<InterestedEventResponseDto> events = interestService.getUserInterestedEvents(userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Interested events retrieved", events)
        );
    }
}
