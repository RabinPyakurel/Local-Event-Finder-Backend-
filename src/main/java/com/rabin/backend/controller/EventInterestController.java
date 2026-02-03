package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.InterestedEventResponseDto;
import com.rabin.backend.service.event.EventInterestService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Event Interest", description = "APIs for marking events as interested (like/heart functionality)")
public class EventInterestController {

    private final EventInterestService interestService;

    @Operation(summary = "Mark event as interested", description = "Mark an event as interested (click heart)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event marked as interested"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{eventId}/interest")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> addInterest(
            @Parameter(description = "Event ID") @PathVariable Long eventId
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

    @Operation(summary = "Remove interest from event", description = "Remove interest from an event (unclick heart)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest removed from event"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{eventId}/interest")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> removeInterest(
            @Parameter(description = "Event ID") @PathVariable Long eventId
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

    @Operation(summary = "Toggle interest", description = "Toggle interest on an event (add if not exists, remove if exists)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest toggled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{eventId}/interest/toggle")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> toggleInterest(
            @Parameter(description = "Event ID") @PathVariable Long eventId
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

    @Operation(summary = "Check interest status", description = "Check if current user is interested in an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest status retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{eventId}/interest/check")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> checkInterest(
            @Parameter(description = "Event ID") @PathVariable Long eventId
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

    @Operation(summary = "Get interest count", description = "Get the total interest count for an event (public endpoint)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest count retrieved"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{eventId}/interest/count")
    public ResponseEntity<GenericApiResponse<Map<String, Long>>> getInterestCount(
            @Parameter(description = "Event ID") @PathVariable Long eventId
    ) {
        log.debug("Get interest count for eventId={}", eventId);

        long count = interestService.getEventInterestCount(eventId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Interest count retrieved", Map.of(
                        "interestCount", count
                ))
        );
    }

    @Operation(summary = "Get user's interested events", description = "Get all events the current user is interested in (for profile page)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interested events retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
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
