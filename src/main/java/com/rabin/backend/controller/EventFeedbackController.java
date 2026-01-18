package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.EventFeedbackRequestDto;
import com.rabin.backend.dto.response.EventFeedbackResponseDto;
import com.rabin.backend.service.event.EventFeedbackService;
import com.rabin.backend.util.SecurityUtil;
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
public class EventFeedbackController {

    private final EventFeedbackService feedbackService;

    public EventFeedbackController(EventFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

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
