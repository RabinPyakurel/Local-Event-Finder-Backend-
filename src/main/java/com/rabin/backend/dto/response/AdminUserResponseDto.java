package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Admin view of user details")
public class AdminUserResponseDto {
    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Date of birth", example = "1995-05-15")
    private LocalDate dob;

    @Schema(description = "Profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String profileImageUrl;

    @Schema(description = "User account status", example = "ACTIVE")
    private String userStatus;

    @Schema(description = "List of user roles", example = "[\"USER\", \"ORGANIZER\"]")
    private List<String> roles;

    @Schema(description = "Account creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime updatedAt;
}
