package com.rabin.backend.controller;

import com.rabin.backend.dto.response.UserFollowResponseDto;
import com.rabin.backend.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing user follow relationships
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    /**
     * Follow a user
     */
    @PostMapping("/{userId}/follow")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> followUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        try {
            Long currentUserId = getCurrentUserId(principal);
            userFollowService.followUser(currentUserId, userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully followed user");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Unfollow a user
     */
    @DeleteMapping("/{userId}/unfollow")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> unfollowUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        try {
            Long currentUserId = getCurrentUserId(principal);
            userFollowService.unfollowUser(currentUserId, userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully unfollowed user");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get list of users that current user is following
     */
    @GetMapping("/following")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getFollowing(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        try {
            Long currentUserId = getCurrentUserId(principal);
            List<UserFollowResponseDto> following = userFollowService.getFollowing(currentUserId);
            return ResponseEntity.ok(following);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get list of followers
     */
    @GetMapping("/followers")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getFollowers(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        try {
            Long currentUserId = getCurrentUserId(principal);
            List<UserFollowResponseDto> followers = userFollowService.getFollowers(currentUserId);
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get follower/following counts
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getUserStats(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        try {
            Long currentUserId = getCurrentUserId(principal);

            Map<String, Long> stats = new HashMap<>();
            stats.put("followers", userFollowService.getFollowerCount(currentUserId));
            stats.put("following", userFollowService.getFollowingCount(currentUserId));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Check if current user is following another user
     */
    @GetMapping("/{userId}/is-following")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> isFollowing(
            @PathVariable Long userId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        try {
            Long currentUserId = getCurrentUserId(principal);
            boolean isFollowing = userFollowService.isFollowing(currentUserId, userId);

            Map<String, Boolean> response = new HashMap<>();
            response.put("isFollowing", isFollowing);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Helper method to get current user ID from principal
     * This is a placeholder - should be replaced with actual implementation
     */
    private Long getCurrentUserId(org.springframework.security.core.userdetails.User principal) {
        // In real implementation, this would extract user ID from JWT or UserDetails
        // For now, this is a placeholder
        return 1L; // TODO: Implement proper user ID extraction
    }
}
