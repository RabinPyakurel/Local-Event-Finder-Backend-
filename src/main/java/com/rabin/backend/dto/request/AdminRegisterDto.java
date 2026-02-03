package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Schema(description = "Admin registration request payload")
public class AdminRegisterDto {
    @Schema(description = "Admin's full name", example = "Admin User", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(description = "Admin's email address", example = "admin@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Admin's password (min 8 characters)", example = "AdminPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Date of birth", example = "1990-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dob;

    @Schema(description = "Profile image file (optional)")
    private MultipartFile profileImage;

    @Schema(description = "Secret key required for admin registration", example = "admin-secret-key", requiredMode = Schema.RequiredMode.REQUIRED)
    private String adminSecretKey;
}
