package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Group details response")
public class GroupResponseDto {
    @Schema(description = "Group ID", example = "1")
    private Long id;

    @Schema(description = "Group name", example = "Tech Enthusiasts Nepal")
    private String name;

    @Schema(description = "Group description", example = "A community for technology enthusiasts")
    private String description;

    @Schema(description = "Group image URL", example = "/uploads/groups/group-1.jpg")
    private String groupImageUrl;

    @Schema(description = "Group creator's user ID", example = "1")
    private Long creatorId;

    @Schema(description = "Group creator's name", example = "John Doe")
    private String creatorName;

    @Schema(description = "Does joining require approval?", example = "false")
    private Boolean requiresApproval;

    @Schema(description = "Is group active?", example = "true")
    private Boolean isActive;

    @Schema(description = "Number of members", example = "150")
    private Long memberCount;

    @Schema(description = "Group tags", example = "[\"TECHNOLOGY\", \"NETWORKING\"]")
    private List<String> tags;

    @Schema(description = "Is current user a member?", example = "true")
    private Boolean isMember;

    @Schema(description = "Is current user the creator?", example = "false")
    private Boolean isCreator;

    @Schema(description = "Group creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime updatedAt;
}
