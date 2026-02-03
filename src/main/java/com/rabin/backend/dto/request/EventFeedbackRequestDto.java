package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Event feedback submission request payload")
public class EventFeedbackRequestDto {
    @Schema(description = "ID of the event to provide feedback for", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long eventId;

    @Schema(description = "Rating from 1 to 5", example = "4", minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Schema(description = "Optional feedback comment", example = "Great event! Well organized and informative.")
    private String comment;
}
