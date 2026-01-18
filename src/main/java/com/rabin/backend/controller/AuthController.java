package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.ChangePasswordDto;
import com.rabin.backend.dto.request.LoginDto;
import com.rabin.backend.dto.request.RegisterDto;
import com.rabin.backend.dto.response.UserAuthResponseDto;
import com.rabin.backend.service.auth.AuthService;
import com.rabin.backend.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericApiResponse<UserAuthResponseDto>> register(@ModelAttribute RegisterDto dto) {
        log.debug("Register endpoint called for email: {}", dto.getEmail());
        UserAuthResponseDto response = authService.registerUser(dto);
        return ResponseEntity.ok(GenericApiResponse.ok(200, "User registered successfully", null));
    }

    @PostMapping("/login")
    public ResponseEntity<GenericApiResponse<UserAuthResponseDto>> login(@RequestBody LoginDto dto) {
        log.debug("Login endpoint called for email: {}", dto.getEmail());
        UserAuthResponseDto response = authService.login(dto);
        return ResponseEntity.ok(GenericApiResponse.ok(200, "Login successful", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<GenericApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordDto dto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Change password requested by userId: {}", userId);

        GenericApiResponse<Void> response = authService.changePassword(dto, userId);
        return ResponseEntity.ok(response);
    }

}
