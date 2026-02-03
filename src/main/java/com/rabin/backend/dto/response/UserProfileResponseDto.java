package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "User profile response")
public class UserProfileResponseDto {
    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Date of birth", example = "1995-05-15")
    private LocalDate dob;

    @Schema(description = "Profile image URL", example = "/uploads/profiles/user-1.jpg")
    private String profileImageUrl;

    @Schema(description = "List of user interests", example = "[\"MUSIC\", \"TECHNOLOGY\"]")
    private List<String> interests;

    @Schema(description = "List of user roles", example = "[\"USER\"]")
    private List<String> roles;

    @Schema(description = "Account creation date", example = "2024-01-15T10:30:00")
    private LocalDateTime joinDate;
}
