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

    @Operation(summary = "Permanently delete event", description = "Permanently delete an event and all related data (enrollments, payments, feedback, interests, reports). Only the event organizer or admin can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event deleted permanently"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this event"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/permanent-delete/{eventId:\\d+}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Void>> deleteEventPermanently(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Permanent delete event request for eventId: {} by userId: {}", eventId, userId);

        eventService.deleteEvent(eventId, userId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Event deleted permanently", null)
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

    @Operation(summary = "Get personalized recommendations (hybrid)", description = "Get event recommendations based on user interests, location, and social connections. Uses a weighted hybrid algorithm.")
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

    @Operation(summary = "Get social recommendations", description = "Get event recommendations based purely on social connections - events that followed users have liked, attended, or created.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Social recommendations fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/recommendations/social")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getSocialRecommendations(
            @Parameter(description = "Maximum number of recommendations") @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get social recommendations request for userId: {}, limit: {}", userId, limit);

        List<EventResponseDto> recommendations = recommendationService.getSocialRecommendations(userId, limit);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Social recommendations fetched successfully", recommendations)
        );
    }

    @Operation(summary = "Get interest-based recommendations", description = "Get event recommendations based purely on user's interests/tags. Uses content similarity matching.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest-based recommendations fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/recommendations/interests")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getInterestBasedRecommendations(
            @Parameter(description = "Maximum number of recommendations") @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get interest-based recommendations request for userId: {}, limit: {}", userId, limit);

        List<EventResponseDto> recommendations = recommendationService.getInterestBasedRecommendations(userId, limit);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Interest-based recommendations fetched successfully", recommendations)
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

    @Operation(summary = "Search events", description = "Search events with filters like location, categories/tags, paid/free, and search term. Public endpoint. Categories: MUSIC_CONCERTS, ART_SHOWS, SPORTS, TECHNOLOGY, FOOD_DRINK, TRAVEL, EDUCATION, OUTDOORS, FITNESS, SPIRITUAL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events fetched successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> searchEvents(
            @Parameter(description = "Latitude for location filter") @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude for location filter") @RequestParam(required = false) Double lon,
            @Parameter(description = "Search radius in km") @RequestParam(required = false) Double radius,
            @Parameter(description = "Filter by tags (e.g., MUSIC_CONCERTS, TECHNOLOGY)") @RequestParam(required = false) List<String> tags,
            @Parameter(description = "Filter by categories - same as tags (e.g., MUSIC_CONCERTS, SPORTS)") @RequestParam(required = false) List<String> categories,
            @Parameter(description = "Filter by paid status: true = paid events only, false = free events only, null = all events") @RequestParam(required = false) Boolean isPaid,
            @Parameter(description = "Search query for title, description, or venue") @RequestParam(required = false) String q
    ) {
        // Merge tags and categories into a single list
        List<String> allTags = new java.util.ArrayList<>();
        if (tags != null) allTags.addAll(tags);
        if (categories != null) allTags.addAll(categories);
        List<String> finalTags = allTags.isEmpty() ? null : allTags;

        log.debug("Search events request - lat={}, lon={}, radius={}, tags={}, categories={}, isPaid={}, query={}",
                lat, lon, radius, tags, categories, isPaid, q);

        List<EventResponseDto> events = eventService.searchEvents(lat, lon, radius, finalTags, q, isPaid);

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

    @Operation(summary = "Get all categories", description = "Get all available event categories. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories fetched successfully")
    })
    @GetMapping("/categories")
    public ResponseEntity<GenericApiResponse<List<java.util.Map<String, String>>>> getCategories() {
        log.debug("Get all categories request");

        List<java.util.Map<String, String>> categories = java.util.Arrays.stream(
                        com.rabin.backend.enums.InterestCategory.values())
                .map(cat -> java.util.Map.of(
                        "key", cat.name(),
                        "displayName", cat.getDisplayName()
                ))
                .toList();

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Categories fetched successfully", categories)
        );
    }

    @Operation(summary = "Get events by category", description = "Get all active events filtered by a specific category. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getEventsByCategory(
            @Parameter(description = "Category key (e.g., MUSIC_CONCERTS, SPORTS, TECHNOLOGY)") @PathVariable String category
    ) {
        log.debug("Get events by category request - category={}", category);

        // Validate category
        try {
            com.rabin.backend.enums.InterestCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    GenericApiResponse.error(400, "Invalid category: " + category)
            );
        }

        List<EventResponseDto> events = eventService.searchEvents(null, null, null, List.of(category.toUpperCase()), null);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Events fetched successfully", events)
        );
    }

    @Operation(summary = "Get upcoming events", description = "Get active events starting in the future, sorted by soonest first. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upcoming events fetched successfully")
    })
    @GetMapping("/upcoming")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getUpcomingEvents() {
        log.debug("Get upcoming events request");
        List<EventResponseDto> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Upcoming events fetched successfully", events)
        );
    }

    @Operation(summary = "Get popular events", description = "Get active events sorted by popularity (interest count + booked seats). Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Popular events fetched successfully")
    })
    @GetMapping("/popular")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getPopularEvents() {
        log.debug("Get popular events request");
        List<EventResponseDto> events = eventService.getPopularEvents();
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Popular events fetched successfully", events)
        );
    }

    @Operation(summary = "Get paid events", description = "Get all active paid events. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paid events fetched successfully")
    })
    @GetMapping("/paid")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getPaidEvents() {
        log.debug("Get paid events request");

        List<EventResponseDto> events = eventService.searchEvents(null, null, null, null, null, true);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Paid events fetched successfully", events)
        );
    }

    @Operation(summary = "Get free events", description = "Get all active free events. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Free events fetched successfully")
    })
    @GetMapping("/free")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getFreeEvents() {
        log.debug("Get free events request");

        List<EventResponseDto> events = eventService.searchEvents(null, null, null, null, null, false);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Free events fetched successfully", events)
        );
    }
}
