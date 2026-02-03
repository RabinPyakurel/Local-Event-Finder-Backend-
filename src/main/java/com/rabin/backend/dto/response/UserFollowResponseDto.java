package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "User follow relationship details")
public class UserFollowResponseDto {
    @Schema(description = "Follow relationship ID", example = "1")
    private Long id;

    @Schema(description = "Followed/follower user ID", example = "1")
    private Long userId;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String profileImageUrl;

    @Schema(description = "Follow timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime followedAt;
}
