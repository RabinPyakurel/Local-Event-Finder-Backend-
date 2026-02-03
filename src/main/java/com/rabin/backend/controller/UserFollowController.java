package com.rabin.backend.controller;

import com.rabin.backend.dto.response.UserFollowResponseDto;
import com.rabin.backend.service.UserFollowService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Follow", description = "APIs for following/unfollowing users and managing social connections")
@SecurityRequirement(name = "bearerAuth")
public class UserFollowController {

    private final UserFollowService userFollowService;

    @Operation(summary = "Follow user", description = "Follow another user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully followed user"),
            @ApiResponse(responseCode = "400", description = "Cannot follow yourself or already following"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{userId}/follow")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> followUser(
            @Parameter(description = "User ID to follow") @PathVariable Long userId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        userFollowService.followUser(currentUserId, userId);

        return ResponseEntity.ok(Map.of("message", "Successfully followed user"));
    }

    @Operation(summary = "Unfollow user", description = "Unfollow a user you are currently following")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unfollowed user"),
            @ApiResponse(responseCode = "400", description = "Not following this user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}/unfollow")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> unfollowUser(
            @Parameter(description = "User ID to unfollow") @PathVariable Long userId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        userFollowService.unfollowUser(currentUserId, userId);

        return ResponseEntity.ok(Map.of("message", "Successfully unfollowed user"));
    }

    @Operation(summary = "Get following list", description = "Get the list of users that the current user is following")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Following list retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/following")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<UserFollowResponseDto>> getFollowing() {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(userFollowService.getFollowing(currentUserId));
    }

    @Operation(summary = "Get followers list", description = "Get the list of users following the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Followers list retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/followers")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<UserFollowResponseDto>> getFollowers() {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(userFollowService.getFollowers(currentUserId));
    }

    @Operation(summary = "Get follow stats", description = "Get follower and following counts for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getUserStats() {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        return ResponseEntity.ok(
                Map.of(
                        "followers", userFollowService.getFollowerCount(currentUserId),
                        "following", userFollowService.getFollowingCount(currentUserId)
                )
        );
    }

    @Operation(summary = "Check if following", description = "Check if the current user is following a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/is-following")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @Parameter(description = "User ID to check") @PathVariable Long userId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean isFollowing = userFollowService.isFollowing(currentUserId, userId);

        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }
}
