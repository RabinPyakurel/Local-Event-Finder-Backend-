package com.rabin.backend.dto.request;

import lombok.Data;

@Data
public class ChangePasswordDto {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
    private boolean adminAction = false;
}
