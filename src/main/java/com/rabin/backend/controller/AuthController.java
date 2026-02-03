package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.ChangePasswordDto;
import com.rabin.backend.dto.request.LoginDto;
import com.rabin.backend.dto.request.RegisterDto;
import com.rabin.backend.dto.response.UserAuthResponseDto;
import com.rabin.backend.service.auth.AuthService;
import com.rabin.backend.util.JwtService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@Tag(name = "Authentication", description = "User authentication and account management APIs")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Register new user", description = "Register a new user account with profile image upload support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = GenericApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    })
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericApiResponse<UserAuthResponseDto>> register(@ModelAttribute RegisterDto dto) {
        log.debug("Register endpoint called for email: {}", dto.getEmail());
        authService.registerUser(dto);
        return ResponseEntity.ok(GenericApiResponse.ok(200, "User registered successfully", null));
    }

    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = GenericApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<GenericApiResponse<UserAuthResponseDto>> login(@RequestBody LoginDto dto) {
        log.debug("Login endpoint called for email: {}", dto.getEmail());
        UserAuthResponseDto response = authService.login(dto);
        return ResponseEntity.ok(GenericApiResponse.ok(200, "Login successful", response));
    }

    @Operation(summary = "Change password", description = "Change password for authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password or password mismatch"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - not logged in")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/change-password")
    public ResponseEntity<GenericApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordDto dto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Change password requested by userId: {}", userId);

        GenericApiResponse<Void> response = authService.changePassword(dto, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Invalidate current JWT token and logout user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<GenericApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtService.invalidateToken(token);
            log.info("User logged out, token invalidated");
        }

        return ResponseEntity.ok(GenericApiResponse.ok(200, "Logged out successfully", null));
    }
}
