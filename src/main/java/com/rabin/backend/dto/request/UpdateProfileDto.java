package com.rabin.backend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class UpdateProfileDto {
    private String fullName;
    private LocalDate dob;
    private MultipartFile profileImage;
}
