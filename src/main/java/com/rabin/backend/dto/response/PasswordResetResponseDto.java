package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Password reset response")
public class PasswordResetResponseDto {
    @Schema(description = "Response message", example = "Password reset email sent successfully")
    private String message;
}
