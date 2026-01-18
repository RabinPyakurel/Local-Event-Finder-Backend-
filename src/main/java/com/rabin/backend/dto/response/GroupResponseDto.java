package com.rabin.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for group information
 */
@Getter
@Setter
public class GroupResponseDto {
    private Long id;
    private String name;
    private String description;
    private String groupImageUrl;
    private Long creatorId;
    private String creatorName;
    private Boolean requiresApproval;
    private Boolean isActive;
    private Long memberCount;
    private List<String> tags;
    private Boolean isMember;  // Whether current user is a member
    private Boolean isCreator;  // Whether current user is the creator
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
