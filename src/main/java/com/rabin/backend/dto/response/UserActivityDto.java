package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "User activity statistics for admin review")
public class UserActivityDto {
    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String profileImageUrl;

    @Schema(description = "Account creation date", example = "2024-01-15T10:30:00")
    private LocalDateTime memberSince;

    @Schema(description = "Number of events attended", example = "15")
    private long eventsAttended;

    @Schema(description = "Number of groups joined", example = "5")
    private long groupsJoined;

    @Schema(description = "Number of followers", example = "150")
    private long followersCount;

    @Schema(description = "Number of users being followed", example = "75")
    private long followingCount;

    @Schema(description = "List of recent event enrollments")
    private List<RecentEnrollmentDto> recentEnrollments;

    @Schema(description = "Account status", example = "ACTIVE")
    private String accountStatus;

    @Schema(description = "Has user been reported?", example = "false")
    private boolean hasActiveReports;

    @Data
    @Schema(description = "Recent enrollment details")
    public static class RecentEnrollmentDto {
        @Schema(description = "Event ID", example = "1")
        private Long eventId;

        @Schema(description = "Event title", example = "Tech Conference 2024")
        private String eventTitle;

        @Schema(description = "Enrollment timestamp", example = "2024-05-20T14:30:00")
        private LocalDateTime enrolledAt;

        @Schema(description = "Ticket status", example = "VALID")
        private String ticketStatus;
    }
}
