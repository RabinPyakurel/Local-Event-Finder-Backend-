package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Change password request payload")
public class ChangePasswordDto {
    @Schema(description = "Current password", example = "OldPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldPassword;

    @Schema(description = "New password (min 8 characters)", example = "NewPassword456!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @Schema(description = "Confirm new password (must match newPassword)", example = "NewPassword456!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

    @Schema(description = "Admin action flag (internal use)", hidden = true)
    private boolean adminAction = false;
}
