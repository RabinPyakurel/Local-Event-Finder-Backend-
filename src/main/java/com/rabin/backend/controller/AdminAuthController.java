package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.AdminRegisterDto;
import com.rabin.backend.dto.request.LoginDto;
import com.rabin.backend.dto.response.UserAuthResponseDto;
import com.rabin.backend.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/admin")
@Slf4j
@Tag(name = "Admin Authentication", description = "Admin registration and login APIs")
public class AdminAuthController {

    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register new admin",
            description = "Register a new admin account. Requires a valid admin secret key for authorization."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin registered successfully",
                    content = @Content(schema = @Schema(implementation = GenericApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "403", description = "Invalid admin secret key")
    })
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericApiResponse<UserAuthResponseDto>> registerAdmin(
            @ModelAttribute AdminRegisterDto dto
    ) {
        log.debug("Admin register endpoint called for email: {}", dto.getEmail());
        UserAuthResponseDto response = authService.registerAdmin(dto);
        return ResponseEntity.ok(GenericApiResponse.ok(200, "Admin registered successfully", response));
    }

    @Operation(
            summary = "Admin login",
            description = "Authenticate admin with email and password. Only users with ADMIN role can login through this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin login successful",
                    content = @Content(schema = @Schema(implementation = GenericApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "User is not an admin")
    })
    @PostMapping("/login")
    public ResponseEntity<GenericApiResponse<UserAuthResponseDto>> adminLogin(@RequestBody LoginDto dto) {
        log.debug("Admin login endpoint called for email: {}", dto.getEmail());
        UserAuthResponseDto response = authService.adminLogin(dto);
        return ResponseEntity.ok(GenericApiResponse.ok(200, "Admin login successful", response));
    }
}
