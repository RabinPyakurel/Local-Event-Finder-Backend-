package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Event feedback response")
public class EventFeedbackResponseDto {
    @Schema(description = "Feedback ID", example = "1")
    private Long id;

    @Schema(description = "Event ID", example = "1")
    private Long eventId;

    @Schema(description = "User ID who submitted the feedback", example = "5")
    private Long userId;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String userName;

    @Schema(description = "Profile image URL of the user", example = "/uploads/profiles/user5.jpg")
    private String userProfileImage;

    @Schema(description = "Whether this feedback belongs to the current logged-in user", example = "true")
    private Boolean isOwner;

    @Schema(description = "Rating from 1 to 5", example = "4")
    private Integer rating;

    @Schema(description = "Feedback comment", example = "Great event! Well organized.")
    private String comment;

    @Schema(description = "Feedback submission timestamp", example = "2024-06-16T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Feedback last updated timestamp", example = "2024-06-17T14:00:00")
    private LocalDateTime updatedAt;
}
