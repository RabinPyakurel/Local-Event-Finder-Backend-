package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "User registration response")
public class UserRegisterResponseDto {
    @Schema(description = "Registration result message", example = "User registered successfully")
    private String message;
}
