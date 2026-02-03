package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Authentication response containing JWT token and user info")
public class UserAuthResponseDto {
    @Schema(description = "JWT access token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Response message", example = "Login successful")
    private String message;

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "List of user roles", example = "[\"USER\", \"ORGANIZER\"]")
    private List<String> roles;
}
