package com.rabin.backend.dto.request;

import com.rabin.backend.enums.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "User registration request payload")
public class RegisterDto {
    @Schema(description = "User's full name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(description = "User's email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "User's password (min 8 characters)", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Date of birth for age validation", example = "1995-05-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dob;

    @Schema(description = "Profile image file (optional)")
    private MultipartFile profileImage;

    @Schema(description = "List of interest category names", example = "[\"MUSIC\", \"SPORTS\", \"TECHNOLOGY\"]")
    private List<String> interests;

    @Schema(description = "User role (defaults to USER if not provided)", example = "USER")
    private RoleName role;
}
