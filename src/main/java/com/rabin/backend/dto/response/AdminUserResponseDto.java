package com.rabin.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private LocalDate dob;
    private String profileImageUrl;
    private String userStatus;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
