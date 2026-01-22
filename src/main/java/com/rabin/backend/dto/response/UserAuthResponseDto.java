package com.rabin.backend.dto.response;

import com.rabin.backend.model.Role;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserAuthResponseDto {
    private String accessToken;
    private String message;
    private String fullName;
    private String email;
    private List<String> roles;
}
