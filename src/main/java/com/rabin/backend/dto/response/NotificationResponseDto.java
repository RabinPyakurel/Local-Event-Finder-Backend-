package com.rabin.backend.dto.response;

import com.rabin.backend.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Notification response")
public class NotificationResponseDto {

    @Schema(description = "Notification ID", example = "1")
    private Long id;

    @Schema(description = "Notification type", example = "EVENT_ENROLLMENT")
    private NotificationType type;

    @Schema(description = "Notification title", example = "New Enrollment")
    private String title;

    @Schema(description = "Notification message", example = "John Doe enrolled in your event 'Tech Conference'")
    private String message;

    @Schema(description = "Whether notification has been read", example = "false")
    private Boolean isRead;

    @Schema(description = "Related entity ID (event ID, user ID, etc.)", example = "42")
    private Long relatedEntityId;

    @Schema(description = "Related entity type", example = "EVENT")
    private String relatedEntityType;

    @Schema(description = "When notification was created", example = "2024-06-15T10:30:00")
    private LocalDateTime createdAt;
}
