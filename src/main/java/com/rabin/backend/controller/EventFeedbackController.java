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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
