package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.ForgotPasswordRequestDto;
import com.rabin.backend.dto.request.ResetPasswordRequestDto;
import com.rabin.backend.dto.response.PasswordResetResponseDto;
import com.rabin.backend.service.auth.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
@Slf4j
@Tag(name = "Password Reset", description = "APIs for password reset functionality - No authentication required")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @Operation(summary = "Request password reset", description = "Send a password reset email to the user's registered email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent (if email exists)"),
            @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    @PostMapping("/forgot")
    public ResponseEntity<GenericApiResponse<PasswordResetResponseDto>> forgotPassword(
            @RequestBody ForgotPasswordRequestDto dto) {
        log.debug("Forgot password endpoint called for email: {}", dto.getEmail());
        return ResponseEntity.ok(passwordResetService.forgotPassword(dto));
    }

    @Operation(summary = "Reset password", description = "Reset the password using the token received via email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "400", description = "Password validation failed")
    })
    @PostMapping("/reset")
    public ResponseEntity<GenericApiResponse<PasswordResetResponseDto>> resetPassword(
            @RequestBody ResetPasswordRequestDto dto) {
        log.debug("Reset password endpoint called with token: {}", dto.getToken());
        return ResponseEntity.ok(passwordResetService.resetPassword(dto));
    }
}
