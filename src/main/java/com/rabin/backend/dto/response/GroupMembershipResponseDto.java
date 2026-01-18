package com.rabin.backend.dto.response;

import com.rabin.backend.enums.MembershipStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for group membership information
 */
@Getter
@Setter
public class GroupMembershipResponseDto {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userProfileImageUrl;
    private Long groupId;
    private String groupName;
    private MembershipStatus status;
    private Boolean isAdmin;
    private LocalDateTime joinedAt;
}
