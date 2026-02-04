package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.EventFeedbackRequestDto;
import com.rabin.backend.dto.response.EventFeedbackResponseDto;
import com.rabin.backend.service.event.EventFeedbackService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@Slf4j
@Tag(name = "Event Feedback", description = "APIs for submitting feedback on attended events")
@SecurityRequirement(name = "bearerAuth")
public class EventFeedbackController {

    private final EventFeedbackService feedbackService;

    public EventFeedbackController(EventFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @Operation(summary = "Submit event feedback", description = "Submit feedback and rating for an attended event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid feedback data or already submitted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "User did not attend this event")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GenericApiResponse<EventFeedbackResponseDto>> submitFeedback(
            @RequestBody EventFeedbackRequestDto dto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Feedback API called by userId {}", userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(
                        200,
                        "Feedback submitted successfully",
                        feedbackService.submitFeedback(userId, dto)
                )
        );
    }

    @Operation(summary = "Update event feedback", description = "Update your existing feedback for an event. Only the feedback owner can update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid feedback data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    @PutMapping("/event/{eventId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GenericApiResponse<EventFeedbackResponseDto>> updateFeedback(
            @Parameter(description = "Event ID") @PathVariable Long eventId,
            @RequestBody EventFeedbackRequestDto dto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Update feedback API called by userId {} for eventId {}", userId, eventId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(
                        200,
                        "Feedback updated successfully",
                        feedbackService.updateFeedback(userId, eventId, dto)
                )
        );
    }

    @Operation(summary = "Delete event feedback", description = "Delete your existing feedback for an event. Only the feedback owner can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    @DeleteMapping("/event/{eventId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GenericApiResponse<Void>> deleteFeedback(
            @Parameter(description = "Event ID") @PathVariable Long eventId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Delete feedback API called by userId {} for eventId {}", userId, eventId);

        feedbackService.deleteFeedback(userId, eventId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Feedback deleted successfully", null)
        );
    }

    @Operation(summary = "Get all feedbacks for an event", description = "Get all feedbacks for a specific event. Each feedback includes an 'isOwner' flag indicating if it belongs to the logged-in user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedbacks fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/event/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<List<EventFeedbackResponseDto>>> getEventFeedbacks(
            @Parameter(description = "Event ID") @PathVariable Long eventId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get feedbacks for eventId {} by userId {}", eventId, userId);

        List<EventFeedbackResponseDto> feedbacks = feedbackService.getEventFeedbacks(eventId, userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Feedbacks fetched successfully", feedbacks)
        );
    }

    @Operation(summary = "Get my feedback for an event", description = "Get the current user's feedback for a specific event. Returns null data if no feedback exists.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/event/{eventId}/mine")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GenericApiResponse<EventFeedbackResponseDto>> getMyFeedback(
            @Parameter(description = "Event ID") @PathVariable Long eventId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get my feedback for eventId {} by userId {}", eventId, userId);

        EventFeedbackResponseDto feedback = feedbackService.getMyFeedback(userId, eventId);

        if (feedback == null) {
            return ResponseEntity.ok(
                    GenericApiResponse.ok(200, "No feedback submitted yet for this event", null)
            );
        }

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Your feedback fetched successfully", feedback)
        );
    }
}
