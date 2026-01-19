package com.rabin.backend.controller;

import com.rabin.backend.dto.response.UserFollowResponseDto;
import com.rabin.backend.service.UserFollowService;
import com.rabin.backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/{userId}/follow")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> followUser(@PathVariable Long userId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        userFollowService.followUser(currentUserId, userId);

        return ResponseEntity.ok(Map.of("message", "Successfully followed user"));
    }

    @DeleteMapping("/{userId}/unfollow")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        userFollowService.unfollowUser(currentUserId, userId);

        return ResponseEntity.ok(Map.of("message", "Successfully unfollowed user"));
    }

    @GetMapping("/following")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<UserFollowResponseDto>> getFollowing() {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(userFollowService.getFollowing(currentUserId));
    }

    @GetMapping("/followers")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<UserFollowResponseDto>> getFollowers() {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(userFollowService.getFollowers(currentUserId));
    }

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

    @GetMapping("/{userId}/is-following")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<Map<String, Boolean>> isFollowing(@PathVariable Long userId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean isFollowing = userFollowService.isFollowing(currentUserId, userId);

        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }
}
