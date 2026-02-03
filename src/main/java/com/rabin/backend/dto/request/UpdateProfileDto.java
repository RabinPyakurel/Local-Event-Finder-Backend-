package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Schema(description = "Update user profile request payload")
public class UpdateProfileDto {
    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Date of birth", example = "1995-05-15")
    private LocalDate dob;

    @Schema(description = "New profile image file")
    private MultipartFile profileImage;
}
