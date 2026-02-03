package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Event enrollment request payload")
public class EventEnrollmentRequestDto {
    @Schema(description = "ID of the event to enroll in", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long eventId;

    @Schema(description = "Number of tickets to reserve (default: 1)", example = "1", defaultValue = "1")
    private Integer numberOfTickets = 1;
}
