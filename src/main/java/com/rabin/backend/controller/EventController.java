package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.CreateEventDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.service.RecommendationService;
import com.rabin.backend.service.event.EventService;
import com.rabin.backend.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Slf4j
public class EventController {

    private final EventService eventService;
    private final RecommendationService recommendationService;

    public EventController(EventService eventService, RecommendationService recommendationService) {
        this.eventService = eventService;
        this.recommendationService = recommendationService;
    }

    // ORGANIZER ONLY - Create Event
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<GenericApiResponse<EventResponseDto>> createEvent(
            @ModelAttribute CreateEventDto dto
    ) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Create event request by organizerId: {}", organizerId);

        EventResponseDto event = eventService.createEvent(dto, organizerId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(201, "Event created successfully", event)
        );
    }

    // ORGANIZER ONLY - Update Event
    @PutMapping(value = "/{eventId:\\d+}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<GenericApiResponse<EventResponseDto>> updateEvent(
            @PathVariable Long eventId,
            @ModelAttribute CreateEventDto dto
    ) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Update event request for eventId: {} by organizerId: {}", eventId, organizerId);

        EventResponseDto event = eventService.updateEvent(eventId, dto, organizerId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event updated successfully", event)
        );
    }

    // ORGANIZER ONLY - Cancel Event
    @DeleteMapping("/{eventId:\\d+}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<GenericApiResponse<Void>> cancelEvent(@PathVariable Long eventId) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Cancel event request for eventId: {} by organizerId: {}", eventId, organizerId);

        eventService.cancelEvent(eventId, organizerId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event cancelled successfully", null)
        );
    }

    // PUBLIC - Get All Active Events
    @GetMapping
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getAllEvents() {
        log.debug("Get all active events request");
        List<EventResponseDto> events = eventService.getActiveEvents();
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Events fetched successfully", events)
        );
    }

    // PUBLIC - Get Event by ID
    @GetMapping("/{eventId:\\d+}")
    public ResponseEntity<GenericApiResponse<EventResponseDto>> getEventById(@PathVariable Long eventId) {
        log.debug("Get event by ID request for eventId: {}", eventId);
        EventResponseDto event = eventService.getEventById(eventId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event fetched successfully", event)
        );
    }

    // AUTHENTICATED - Get Personalized Recommendations
    @GetMapping("/recommendations")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getRecommendations(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get recommendations request for userId: {}, lat: {}, lon: {}, limit: {}",
                  userId, lat, lon, limit);

        List<EventResponseDto> recommendations = recommendationService.getRecommendations(
                userId, lat, lon, limit
        );

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Recommendations fetched successfully", recommendations)
        );
    }

    // PUBLIC - Explore Events (sorted by date)
    @GetMapping("/explore")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> exploreEvents() {
        log.debug("Explore events request");
        List<EventResponseDto> events = eventService.getActiveEvents();
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Events fetched successfully", events)
        );
    }

    // ORGANIZER - Get My Events (events created by logged-in organizer)
    @GetMapping("/my-events")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getMyEvents() {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Get my events request for organizerId: {}", organizerId);

        List<EventResponseDto> events = eventService.getOrganizerEvents(organizerId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Your events fetched successfully", events)
        );
    }

    // PUBLIC - Search Events with Filters
    @GetMapping("/search")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> searchEvents(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String q
    ) {
        log.debug("Search events request - lat={}, lon={}, radius={}, tags={}, query={}",
                lat, lon, radius, tags, q);

        List<EventResponseDto> events = eventService.searchEvents(lat, lon, radius, tags, q);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Events fetched successfully", events)
        );
    }
}
