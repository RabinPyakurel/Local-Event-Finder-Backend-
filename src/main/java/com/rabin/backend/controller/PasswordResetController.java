package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.ForgotPasswordRequestDto;
import com.rabin.backend.dto.request.ResetPasswordRequestDto;
import com.rabin.backend.dto.response.PasswordResetResponseDto;
import com.rabin.backend.service.auth.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<GenericApiResponse<PasswordResetResponseDto>> forgotPassword(
            @RequestBody ForgotPasswordRequestDto dto) {
        log.debug("Forgot password endpoint called for email: {}", dto.getEmail());
        return ResponseEntity.ok(passwordResetService.forgotPassword(dto));
    }

    @PostMapping("/reset")
    public ResponseEntity<GenericApiResponse<PasswordResetResponseDto>> resetPassword(
            @RequestBody ResetPasswordRequestDto dto) {
        log.debug("Reset password endpoint called with token: {}", dto.getToken());
        return ResponseEntity.ok(passwordResetService.resetPassword(dto));
    }
}
