package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Public user profile response")
public class PublicProfileResponseDto {
    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String profileImageUrl;

    @Schema(description = "List of user interests", example = "[\"MUSIC\", \"TECHNOLOGY\"]")
    private List<String> interests;

    @Schema(description = "List of user roles", example = "[\"USER\", \"ORGANIZER\"]")
    private List<String> roles;

    @Schema(description = "Account creation date", example = "2024-01-15T10:30:00")
    private LocalDateTime joinDate;

    @Schema(description = "Number of followers", example = "150")
    private Long followerCount;

    @Schema(description = "Number of users being followed", example = "75")
    private Long followingCount;

    @Schema(description = "Is current user following this profile?", example = "true")
    private Boolean isFollowing;

    @Schema(description = "Number of events created (for organizers)", example = "10")
    private Long eventsCreatedCount;

    @Schema(description = "Number of events user is interested in", example = "25")
    private Long interestedEventsCount;
}
