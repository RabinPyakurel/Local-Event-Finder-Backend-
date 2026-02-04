package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.enums.InterestCategory;
import com.rabin.backend.repository.EventTagRepository;
import com.rabin.backend.repository.GroupRepository;
import com.rabin.backend.service.PublicStatsService;
import com.rabin.backend.service.event.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Public", description = "Public APIs - No authentication required")
public class PublicController {

    private final GroupRepository groupRepository;
    private final EventTagRepository eventTagRepository;
    private final EventService eventService;
    private final PublicStatsService publicStatsService;

    @Operation(summary = "Get all interest categories", description = "Get all available interest categories for event tagging and user preferences")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest categories retrieved successfully")
    })
    @GetMapping("/interests")
    public ResponseEntity<GenericApiResponse<List<Map<String, String>>>> getAllInterests() {
        log.debug("Public request for all interest categories");

        List<Map<String, String>> interests = Arrays.stream(InterestCategory.values())
                .map(category -> {
                    Map<String, String> interest = new HashMap<>();
                    interest.put("name", category.name());
                    interest.put("displayName", category.getDisplayName());
                    return interest;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(GenericApiResponse.ok(200,
                "Interest categories retrieved successfully", interests));
    }

    @Operation(summary = "Get all event tags", description = "Get all available event tags")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event tags retrieved successfully")
    })
    @GetMapping("/tags")
    public ResponseEntity<GenericApiResponse<List<Map<String, Object>>>> getAllTags() {
        log.debug("Public request for all event tags");

        List<Map<String, Object>> tags = eventTagRepository.findAll().stream()
                .map(tag -> {
                    Map<String, Object> tagMap = new HashMap<>();
                    tagMap.put("id", tag.getId());
                    tagMap.put("name", tag.getDisplayName());
                    tagMap.put("tagKey", tag.getTagKey());
                    return tagMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(GenericApiResponse.ok(200,
                "Event tags retrieved successfully", tags));
    }

    @Operation(summary = "Get all public groups", description = "Get all active groups available for joining")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    @GetMapping("/groups")
    public ResponseEntity<GenericApiResponse<List<Map<String, Object>>>> getAllPublicGroups() {
        log.debug("Public request for all active groups");

        List<Map<String, Object>> groups = groupRepository.findByIsActiveTrue().stream()
                .map(group -> {
                    Map<String, Object> groupMap = new HashMap<>();
                    groupMap.put("id", group.getId());
                    groupMap.put("name", group.getName());
                    groupMap.put("description", group.getDescription());
                    groupMap.put("groupImageUrl", group.getGroupImageUrl());
                    groupMap.put("requiresApproval", group.getRequiresApproval());
                    groupMap.put("createdAt", group.getCreatedAt());
                    return groupMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(GenericApiResponse.ok(200,
                "Groups retrieved successfully", groups));
    }

    @Operation(summary = "Get public events", description = "Get all active events with optional filters for location, tags, and search term")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
    })
    @GetMapping("/events")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getPublicEvents(
            @Parameter(description = "Latitude for location filter") @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitude for location filter") @RequestParam(required = false) Double lon,
            @Parameter(description = "Search radius in km") @RequestParam(required = false) Double radius,
            @Parameter(description = "Filter by tags") @RequestParam(required = false) List<String> tags,
            @Parameter(description = "Search query") @RequestParam(required = false) String q
    ) {
        log.debug("Public events request - lat={}, lon={}, radius={}, tags={}, query={}",
                lat, lon, radius, tags, q);

        List<EventResponseDto> events;

        // If any filters are provided, use search
        if (lat != null || lon != null || tags != null || q != null) {
            events = eventService.searchEvents(lat, lon, radius, tags, q);
        } else {
            // Otherwise, return all active events
            events = eventService.getActiveEvents();
        }

        return ResponseEntity.ok(GenericApiResponse.ok(200,
                "Events retrieved successfully", events));
    }

    @Operation(summary = "Get nearby events", description = "Get events within a specified radius from the given location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nearby events retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Latitude and longitude are required")
    })
    @GetMapping("/events/nearby")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getNearbyEvents(
            @Parameter(description = "Latitude", required = true) @RequestParam Double lat,
            @Parameter(description = "Longitude", required = true) @RequestParam Double lon,
            @Parameter(description = "Search radius in km (default: 50)") @RequestParam(required = false, defaultValue = "50") Double radius
    ) {
        log.debug("Public nearby events request - lat={}, lon={}, radius={}km", lat, lon, radius);

        if (lat == null || lon == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        List<EventResponseDto> events = eventService.getEventsByLocation(lat, lon, radius);

        return ResponseEntity.ok(GenericApiResponse.ok(200,
                "Nearby events retrieved successfully", events));
    }

    @GetMapping("/stats")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> getPublicStats() {
        log.debug("Admin: Get dashboard statistics request");
        GenericApiResponse<Map<String, Object>> response = publicStatsService.getPublicStats();
        return ResponseEntity.ok(response);
    }
}
