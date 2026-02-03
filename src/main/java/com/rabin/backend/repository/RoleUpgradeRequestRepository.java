package com.rabin.backend.repository;

import com.rabin.backend.enums.RoleUpgradeStatus;
import com.rabin.backend.model.RoleUpgradeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleUpgradeRequestRepository extends JpaRepository<RoleUpgradeRequest, Long> {

    // Find pending request for a user
    Optional<RoleUpgradeRequest> findByUser_IdAndStatus(Long userId, RoleUpgradeStatus status);

    // Check if user has pending request
    boolean existsByUser_IdAndStatus(Long userId, RoleUpgradeStatus status);

    // Find all requests by status
    List<RoleUpgradeRequest> findByStatus(RoleUpgradeStatus status);

    // Find all requests by status with pagination
    Page<RoleUpgradeRequest> findByStatus(RoleUpgradeStatus status, Pageable pageable);

    // Find all requests for a user
    List<RoleUpgradeRequest> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // Count pending requests (for admin notification)
    long countByStatus(RoleUpgradeStatus status);
}
