package com.rabin.backend.dto.request;

import lombok.Data;

@Data
public class ResetPasswordRequestDto {
    private String token;
    private String newPassword;
    private String confirmPassword;
}
