package com.rabin.backend.dto.response;

import com.rabin.backend.enums.MembershipStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Group membership details response")
public class GroupMembershipResponseDto {
    @Schema(description = "Membership ID", example = "1")
    private Long id;

    @Schema(description = "Member's user ID", example = "1")
    private Long userId;

    @Schema(description = "Member's full name", example = "John Doe")
    private String userFullName;

    @Schema(description = "Member's email", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Member's profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String userProfileImageUrl;

    @Schema(description = "Group ID", example = "1")
    private Long groupId;

    @Schema(description = "Group name", example = "Tech Enthusiasts Nepal")
    private String groupName;

    @Schema(description = "Membership status", example = "ACTIVE")
    private MembershipStatus status;

    @Schema(description = "Is member a group admin?", example = "false")
    private Boolean isAdmin;

    @Schema(description = "Join timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime joinedAt;
}
