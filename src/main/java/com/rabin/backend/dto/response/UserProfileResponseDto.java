package com.rabin.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserProfileResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private LocalDate dob;
    private String profileImageUrl;
    private List<String> interests;  // List of interest category names
    private List<String> roles;  // List of role names
}
