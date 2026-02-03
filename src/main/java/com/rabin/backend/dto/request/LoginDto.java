package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Login request payload")
public class LoginDto {
    @Schema(description = "User's email address", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "User's password", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}