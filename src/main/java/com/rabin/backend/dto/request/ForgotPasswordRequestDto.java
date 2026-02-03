package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Forgot password request payload")
public class ForgotPasswordRequestDto {
    @Schema(description = "Email address associated with the account", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
