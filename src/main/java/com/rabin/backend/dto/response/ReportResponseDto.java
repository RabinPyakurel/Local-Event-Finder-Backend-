package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Event report response")
public class ReportResponseDto {
    @Schema(description = "Report ID", example = "1")
    private Long id;

    @Schema(description = "Reporter's user ID", example = "1")
    private Long reporterId;

    @Schema(description = "Reporter's name", example = "John Doe")
    private String reporterName;

    @Schema(description = "Reporter's email", example = "john.doe@example.com")
    private String reporterEmail;

    @Schema(description = "Reported event ID", example = "1")
    private Long eventId;

    @Schema(description = "Reported event title", example = "Tech Conference 2024")
    private String eventTitle;

    @Schema(description = "Report reason", example = "Inappropriate content")
    private String reason;

    @Schema(description = "Report status", example = "PENDING")
    private String reportStatus;

    @Schema(description = "Report creation timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime createdAt;
}
