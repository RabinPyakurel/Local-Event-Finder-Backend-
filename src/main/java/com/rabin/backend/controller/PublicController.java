package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.enums.InterestCategory;
import com.rabin.backend.repository.EventTagRepository;
import com.rabin.backend.repository.GroupRepository;
import com.rabin.backend.service.event.EventService;
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
public class PublicController {

    private final GroupRepository groupRepository;
    private final EventTagRepository eventTagRepository;
    private final EventService eventService;

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

    /**
     * Get all active events (no authentication required)
     * Can filter by location, tags, and search term
     */
    @GetMapping("/events")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getPublicEvents(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String q
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

    /**
     * Get events by location (no authentication required)
     * Specifically for location-based filtering
     */
    @GetMapping("/events/nearby")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getNearbyEvents(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false, defaultValue = "50") Double radius
    ) {
        log.debug("Public nearby events request - lat={}, lon={}, radius={}km", lat, lon, radius);

        if (lat == null || lon == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        List<EventResponseDto> events = eventService.getEventsByLocation(lat, lon, radius);

        return ResponseEntity.ok(GenericApiResponse.ok(200,
                "Nearby events retrieved successfully", events));
    }
}
