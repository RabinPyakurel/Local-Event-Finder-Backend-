package com.rabin.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PublicProfileResponseDto {
    private Long id;
    private String fullName;
    private String profileImageUrl;
    private List<String> interests;
    private List<String> roles;
    private LocalDateTime joinDate;

    // Follow stats
    private Long followerCount;
    private Long followingCount;

    // For authenticated users - whether current user follows this profile
    private Boolean isFollowing;

    // Organizer stats (only populated if user is an organizer)
    private Long eventsCreatedCount;

    // User's interested events count
    private Long interestedEventsCount;
}
