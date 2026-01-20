package com.rabin.backend.dto.response;

import lombok.Data;

@Data
public class UserAuthResponseDto {
    private String accessToken;
    private String message;
    private String fullName;
    private String email;
    private String role;
}
