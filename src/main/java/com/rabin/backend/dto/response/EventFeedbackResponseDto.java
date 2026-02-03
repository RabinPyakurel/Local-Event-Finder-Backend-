package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Event feedback response")
public class EventFeedbackResponseDto {
    @Schema(description = "Event ID", example = "1")
    private Long eventId;

    @Schema(description = "Rating from 1 to 5", example = "4")
    private Integer rating;

    @Schema(description = "Feedback comment", example = "Great event! Well organized.")
    private String comment;

    @Schema(description = "Feedback submission timestamp", example = "2024-06-16T10:30:00")
    private LocalDateTime createdAt;
}
