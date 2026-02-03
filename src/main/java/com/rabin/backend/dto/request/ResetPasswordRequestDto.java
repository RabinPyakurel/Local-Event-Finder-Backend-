package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Reset password request payload")
public class ResetPasswordRequestDto {
    @Schema(description = "Password reset token received via email", example = "abc123xyz789", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @Schema(description = "New password (min 8 characters)", example = "NewSecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @Schema(description = "Confirm new password (must match newPassword)", example = "NewSecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}
