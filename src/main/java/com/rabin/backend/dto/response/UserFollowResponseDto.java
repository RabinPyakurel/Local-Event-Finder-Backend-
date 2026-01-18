package com.rabin.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for user follow relationship
 */
@Getter
@Setter
public class UserFollowResponseDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String profileImageUrl;
    private LocalDateTime followedAt;
}
