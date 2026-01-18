package com.rabin.backend.repository;

import com.rabin.backend.model.Group;
import com.rabin.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    // Find all active groups
    List<Group> findByIsActiveTrue();

    // Find groups created by a specific user
    List<Group> findByCreatedBy(User user);

    // Find group by ID and creator (for ownership verification)
    Optional<Group> findByIdAndCreatedBy(Long groupId, User creator);

    // Search groups by name
    List<Group> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}
