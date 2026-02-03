package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.CreateEventDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.service.RecommendationService;
import com.rabin.backend.service.event.EventService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Events", description = "Event management APIs - Create, update, delete, and browse events")
public class EventController {

    private final EventService eventService;
    private final RecommendationService recommendationService;

    public EventController(EventService eventService, RecommendationService recommendationService) {
        this.eventService = eventService;
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Create new event", description = "Create a new event. Only organizers can create events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully",
                    content = @Content(schema = @Schema(implementation = GenericApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "User is not an organizer")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/create")
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

    @Operation(summary = "Update event", description = "Update an existing event. Only the event organizer can update it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this event"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(value = "/update/{eventId:\\d+}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<GenericApiResponse<EventResponseDto>> updateEvent(
            @Parameter(description = "Event ID") @PathVariable Long eventId,
            @ModelAttribute CreateEventDto dto
    ) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Update event request for eventId: {} by organizerId: {}", eventId, organizerId);

        EventResponseDto event = eventService.updateEvent(eventId, dto, organizerId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event updated successfully", event)
        );
    }

    @Operation(summary = "Cancel event", description = "Cancel an event. Enrolled users will be notified and refunded if applicable.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event cancelled successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to cancel this event"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/delete/{eventId:\\d+}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<GenericApiResponse<Void>> cancelEvent(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        Long organizerId = SecurityUtil.getCurrentUserId();
        log.debug("Cancel event request for eventId: {} by organizerId: {}", eventId, organizerId);

        eventService.cancelEvent(eventId, organizerId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event cancelled successfully", null)
        );
    }

    @Operation(summary = "Get all active events", description = "Retrieve all active events. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events fetched successfully")
    })
    @GetMapping
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getAllEvents() {
        log.debug("Get all active events request");
        List<EventResponseDto> events = eventService.getActiveEvents();
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Events fetched successfully", events)
        );
    }

    @Operation(summary = "Get event by ID", description = "Retrieve a specific event by its ID. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{eventId:\\d+}")
    public ResponseEntity<GenericApiResponse<EventResponseDto>> getEventById(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        log.debug("Get event by ID request for eventId: {}", eventId);
        EventResponseDto event = eventService.getEventById(eventId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event fetched successfully", event)
        );
    }

    @Operation(summary = "Get personalized recommendations", description = "Get event recommendations based on user interests, location, and social connections.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/recommendations")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getRecommendations(
            @Parameter(description = "User latitude") @RequestParam(required = false) Double lat,
            @Parameter(description = "User longitude") @RequestParam(required = false) Double lon,
            @Parameter(description = "Maximum number of recommendations") @RequestParam(required = false, defaultValue = "10") Integer limit
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

    @Operation(summary = "Explore events", description = "Browse all active events sorted by date. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events fetched successfully")
    })
    @GetMapping("/explore")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> exploreEvents() {
        log.debug("Explore events request");
        List<EventResponseDto> events = eventService.getActiveEvents();
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Events fetched successfully", events)
        );
    }

    @Operation(summary = "Get my events", description = "Get all events created by the logged-in organizer. Includes all statuses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "User is not an organizer")
    })
    @SecurityRequirement(name = "bearerAuth")
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

    @Operation(summary = "Search events", description = "Search events with filters like location, tags, and search term. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events fetched successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> searchEvents(
            @Parameter(description = "Latitude for location filter") @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude for location filter") @RequestParam(required = false) Double lon,
            @Parameter(description = "Search radius in km") @RequestParam(required = false) Double radius,
            @Parameter(description = "Filter by tags (e.g., MUSIC_CONCERTS, TECH_MEETUPS)") @RequestParam(required = false) List<String> tags,
            @Parameter(description = "Search query for title, description, or venue") @RequestParam(required = false) String q
    ) {
        log.debug("Search events request - lat={}, lon={}, radius={}, tags={}, query={}",
                lat, lon, radius, tags, q);

        List<EventResponseDto> events = eventService.searchEvents(lat, lon, radius, tags, q);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Events fetched successfully", events)
        );
    }

    @Operation(summary = "Check event ownership", description = "Check if the current user is the organizer of an event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ownership check successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{eventId}/check-ownership")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Boolean>> checkOwnership(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        Long usrId = SecurityUtil.getCurrentUserId();

        boolean isMyEvent = eventService.isEventOrganizer(eventId, usrId);

        return ResponseEntity.ok(GenericApiResponse.ok(200, "Ownership check successful", isMyEvent));
    }
}
