package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Event enrollment response")
public class EventEnrollmentResponseDto {
    @Schema(description = "Event ID", example = "1")
    private Long eventId;

    @Schema(description = "Event title", example = "Tech Conference 2024")
    private String eventTitle;

    @Schema(description = "Enrollment timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime enrolledAt;

    @Schema(description = "Event start date", example = "2024-06-15T10:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Event end date", example = "2024-06-15T18:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Event venue", example = "Kathmandu Convention Center")
    private String venue;

    @Schema(description = "Enrolled user ID", example = "1")
    private Long userId;

    @Schema(description = "Enrolled user's full name", example = "John Doe")
    private String userFullName;

    @Schema(description = "Enrolled user's email", example = "john.doe@example.com")
    private String userEmail;
}
