package com.rabin.backend.repository;

import com.rabin.backend.enums.MembershipStatus;
import com.rabin.backend.model.Group;
import com.rabin.backend.model.GroupMembership;
import com.rabin.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    // Find membership by user and group
    Optional<GroupMembership> findByUserAndGroup(User user, Group group);

    // Check if user is member of group
    boolean existsByUserAndGroupAndStatus(User user, Group group, MembershipStatus status);

    // Get all members of a group
    List<GroupMembership> findByGroupAndStatus(Group group, MembershipStatus status);

    // Get all groups a user is member of
    List<GroupMembership> findByUserAndStatus(User user, MembershipStatus status);

    // Count active members in a group
    long countByGroupAndStatus(Group group, MembershipStatus status);

    // Count groups a user is member of
    long countByUserAndStatus(User user, MembershipStatus status);

    List<GroupMembership> findByGroup(Group group);

    // Delete all memberships for a group
    void deleteByGroup(Group group);
}
