package com.rabin.backend.dto.response;

import com.rabin.backend.enums.RoleUpgradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Role upgrade request response")
public class RoleUpgradeResponseDto {
    @Schema(description = "Request ID", example = "1")
    private Long id;

    @Schema(description = "Requesting user's ID", example = "1")
    private Long userId;

    @Schema(description = "Requesting user's full name", example = "John Doe")
    private String userFullName;

    @Schema(description = "Requesting user's email", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Requesting user's profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String userProfileImageUrl;

    @Schema(description = "Reason for requesting organizer role", example = "I want to organize tech meetups")
    private String reason;

    @Schema(description = "Request status", example = "PENDING")
    private RoleUpgradeStatus status;

    @Schema(description = "Admin's note on the decision", example = "Approved based on user activity")
    private String adminNote;

    @Schema(description = "Admin who reviewed the request", example = "2")
    private Long reviewedById;

    @Schema(description = "Reviewing admin's name", example = "Admin User")
    private String reviewedByName;

    @Schema(description = "Request creation timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Review timestamp", example = "2024-05-21T10:00:00")
    private LocalDateTime reviewedAt;
}
