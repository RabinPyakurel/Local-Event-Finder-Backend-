package com.rabin.backend.repository;

import com.rabin.backend.model.User;
import com.rabin.backend.model.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    // Check if follower is following another user
    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    boolean existsByFollowerAndFollowing(User follower, User following);

    // Get all users that a user is following
    List<UserFollow> findByFollower_Id(Long followerId);

    List<UserFollow> findByFollower(User follower);

    // Get all followers of a user
    List<UserFollow> findByFollowing_Id(Long followingId);

    List<UserFollow> findByFollowing(User following);

    // Find specific follow relationship
    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);

    // Delete follow relationship
    void deleteByFollowerAndFollowing(User follower, User following);

    // Count followers and following
    long countByFollowing(User following);

    long countByFollower(User follower);
}
