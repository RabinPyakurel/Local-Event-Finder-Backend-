package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.UpdateInterestsDto;
import com.rabin.backend.dto.request.UpdateProfileDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.dto.response.PublicProfileResponseDto;
import com.rabin.backend.dto.response.UserProfileResponseDto;
import com.rabin.backend.service.UserService;
import com.rabin.backend.service.event.EventService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Tag(name = "User Profile", description = "User profile management APIs")
public class UserController {

    private final UserService userService;
    private final EventService eventService;

    public UserController(UserService userService, EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    @Operation(summary = "Get my profile", description = "Get the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully",
                    content = @Content(schema = @Schema(implementation = GenericApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public ResponseEntity<GenericApiResponse<UserProfileResponseDto>> getProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get profile request for user: {}", userId);
        GenericApiResponse<UserProfileResponseDto> response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update my profile", description = "Update the profile of the currently authenticated user including profile image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(value = "/profile", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericApiResponse<UserProfileResponseDto>> updateProfile(
            @ModelAttribute UpdateProfileDto dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Update profile request for user: {}", userId);
        GenericApiResponse<UserProfileResponseDto> response = userService.updateProfile(userId, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get my interests", description = "Get the interest categories of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interests fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/interests")
    public ResponseEntity<GenericApiResponse<List<String>>> getInterests() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get interests request for user: {}", userId);
        GenericApiResponse<List<String>> response = userService.getUserInterests(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update my interests", description = "Update the interest categories of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interests updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid interest categories"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/interests")
    public ResponseEntity<GenericApiResponse<List<String>>> updateInterests(
            @RequestBody UpdateInterestsDto dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Update interests request for user: {}", userId);
        GenericApiResponse<List<String>> response = userService.updateUserInterests(userId, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get public profile", description = "Get public profile for any user. If authenticated, includes follow status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/public")
    public ResponseEntity<GenericApiResponse<PublicProfileResponseDto>> getPublicProfile(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.debug("Get public profile request for user: {}", userId);

        // Try to get current user ID if authenticated (optional)
        Long currentUserId = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                currentUserId = SecurityUtil.getCurrentUserId();
            }
        } catch (Exception e) {
            // Not authenticated, that's fine for public profile
            log.debug("Public profile accessed without authentication");
        }

        GenericApiResponse<PublicProfileResponseDto> response = userService.getPublicProfile(userId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get organizer's public events", description = "Get all active events created by a specific organizer. Public endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/events")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getOrganizerPublicEvents(
            @Parameter(description = "User/Organizer ID") @PathVariable Long userId) {
        log.debug("Get public events request for organizer: {}", userId);

        List<EventResponseDto> events = eventService.getOrganizerPublicEvents(userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Organizer events fetched successfully", events)
        );
    }
}
