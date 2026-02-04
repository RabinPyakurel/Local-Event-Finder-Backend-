package com.rabin.backend.service;

import com.rabin.backend.dto.response.UserFollowResponseDto;
import com.rabin.backend.enums.NotificationType;
import com.rabin.backend.enums.RoleName;
import com.rabin.backend.model.User;
import com.rabin.backend.model.UserFollow;
import com.rabin.backend.repository.UserFollowRepository;
import com.rabin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user follow relationships
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Follow a user
     */
    @Transactional
    public void followUser(Long followerId, Long userToFollowId) {
        if (followerId.equals(userToFollowId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        User userToFollow = userRepository.findById(userToFollowId)
                .orElseThrow(() -> new RuntimeException("User to follow not found"));

        // Prevent admins from following or being followed
        boolean followerIsAdmin = follower.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ADMIN);
        boolean toFollowIsAdmin = userToFollow.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ADMIN);

        if (followerIsAdmin) {
            throw new IllegalStateException("Administrators cannot follow users");
        }

        if (toFollowIsAdmin) {
            throw new IllegalStateException("Cannot follow administrators");
        }

        if (userFollowRepository.existsByFollowerAndFollowing(follower, userToFollow)) {
            throw new IllegalStateException("Already following this user");
        }

        UserFollow userFollow = new UserFollow();
        userFollow.setFollower(follower);
        userFollow.setFollowing(userToFollow);

        userFollowRepository.save(userFollow);
        log.info("User {} followed user {}", followerId, userToFollowId);

        // Notify the followed user
        notificationService.sendNotification(
                userToFollowId,
                NotificationType.USER_FOLLOW,
                "New Follower",
                follower.getFullName() + " started following you",
                followerId,
                "USER"
        );
    }

    /**
     * Unfollow a user
     */
    @Transactional
    public void unfollowUser(Long followerId, Long userToUnfollowId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        User userToUnfollow = userRepository.findById(userToUnfollowId)
                .orElseThrow(() -> new RuntimeException("User to unfollow not found"));

        if (!userFollowRepository.existsByFollowerAndFollowing(follower, userToUnfollow)) {
            throw new IllegalStateException("Not following this user");
        }

        userFollowRepository.deleteByFollowerAndFollowing(follower, userToUnfollow);
        log.info("User {} unfollowed user {}", followerId, userToUnfollowId);
    }

    /**
     * Get all users that a user is following
     */
    public List<UserFollowResponseDto> getFollowing(Long userId) {
        List<UserFollow> following = userFollowRepository.findByFollower_Id(userId);

        return following.stream()
                .map(uf -> mapToDto(uf.getFollowing(), uf.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Get all followers of a user
     */
    public List<UserFollowResponseDto> getFollowers(Long userId) {
        List<UserFollow> followers = userFollowRepository.findByFollowing_Id(userId);

        return followers.stream()
                .map(uf -> mapToDto(uf.getFollower(), uf.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Check if user is following another user
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId);
    }

    /**
     * Get follower count
     */
    public long getFollowerCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userFollowRepository.countByFollowing(user);
    }

    /**
     * Get following count
     */
    public long getFollowingCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userFollowRepository.countByFollower(user);
    }

    /**
     * Get list of user IDs that a user follows (for recommendation algorithm)
     */
    public List<Long> getFollowedUserIds(Long userId) {
        List<UserFollow> following = userFollowRepository.findByFollower_Id(userId);
        return following.stream()
                .map(uf -> uf.getFollowing().getId())
                .collect(Collectors.toList());
    }

    /**
     * Map User to UserFollowResponseDto
     */
    private UserFollowResponseDto mapToDto(User user, java.time.LocalDateTime followedAt) {
        UserFollowResponseDto dto = new UserFollowResponseDto();
        dto.setId(user.getId());
        dto.setUserId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setFollowedAt(followedAt);
        return dto;
    }
}
