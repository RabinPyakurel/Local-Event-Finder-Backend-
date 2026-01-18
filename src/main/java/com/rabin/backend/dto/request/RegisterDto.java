package com.rabin.backend.dto.request;

import com.rabin.backend.enums.RoleName;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class RegisterDto {
    private String fullName;
    private String email;
    private String password;
    private LocalDate dob;  // Date of birth for age validation
    private MultipartFile profileImage;  // Optional profile image
    private List<String> interests;  // List of interest category names
    private RoleName role;  // Default to USER if not provided
}
