package com.rabin.backend.service;

import com.rabin.backend.dto.request.RoleUpgradeRequestDto;
import com.rabin.backend.dto.response.RoleUpgradeResponseDto;
import com.rabin.backend.dto.response.UserActivityDto;
import com.rabin.backend.enums.MembershipStatus;
import com.rabin.backend.enums.RoleName;
import com.rabin.backend.enums.RoleUpgradeStatus;
import com.rabin.backend.exception.ResourceNotFoundException;
import com.rabin.backend.model.EventEnrollment;
import com.rabin.backend.model.Role;
import com.rabin.backend.model.RoleUpgradeRequest;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventEnrollmentRepository;
import com.rabin.backend.repository.GroupMembershipRepository;
import com.rabin.backend.repository.RoleRepository;
import com.rabin.backend.repository.RoleUpgradeRequestRepository;
import com.rabin.backend.repository.UserFollowRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleUpgradeService {

    private final RoleUpgradeRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EventEnrollmentRepository enrollmentRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final UserFollowRepository userFollowRepository;
    private final EmailUtil emailUtil;

    public RoleUpgradeService(RoleUpgradeRequestRepository requestRepository,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               EventEnrollmentRepository enrollmentRepository,
                               GroupMembershipRepository groupMembershipRepository,
                               UserFollowRepository userFollowRepository,
                               EmailUtil emailUtil) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.userFollowRepository = userFollowRepository;
        this.emailUtil = emailUtil;
    }

    // ==================== USER METHODS ====================

    /**
     * Submit a request to upgrade to organizer role
     */
    @Transactional
    public RoleUpgradeResponseDto submitRequest(Long userId, RoleUpgradeRequestDto dto) {
        log.debug("Role upgrade request from userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Check if user is already an organizer
        boolean isOrganizer = user.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.ORGANIZER);
        if (isOrganizer) {
            throw new IllegalStateException("You are already an organizer");
        }

        // Check for pending request
        if (requestRepository.existsByUser_IdAndStatus(userId, RoleUpgradeStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending request");
        }

        // Create request
        RoleUpgradeRequest request = new RoleUpgradeRequest();
        request.setUser(user);
        request.setReason(dto.getReason());
        request.setStatus(RoleUpgradeStatus.PENDING);

        requestRepository.save(request);
        log.info("Role upgrade request submitted by userId: {}", userId);

        // Notify all admins
        notifyAdmins(user);

        return mapToResponseDto(request);
    }

    /**
     * Get user's own role upgrade requests
     */
    public List<RoleUpgradeResponseDto> getMyRequests(Long userId) {
        List<RoleUpgradeRequest> requests = requestRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        return requests.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    // ==================== ADMIN METHODS ====================

    /**
     * Get all pending role upgrade requests
     */
    public List<RoleUpgradeResponseDto> getPendingRequests(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.ASC, "createdAt")
        );

        Page<RoleUpgradeRequest> requests = requestRepository.findByStatus(RoleUpgradeStatus.PENDING, pageable);
        return requests.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all role upgrade requests with optional status filter
     */
    public List<RoleUpgradeResponseDto> getAllRequests(RoleUpgradeStatus status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<RoleUpgradeRequest> requests;
        if (status != null) {
            requests = requestRepository.findByStatus(status, pageable);
        } else {
            requests = requestRepository.findAll(pageable);
        }

        return requests.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get user activity for a role upgrade request
     */
    public UserActivityDto getUserActivity(Long requestId) {
        RoleUpgradeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request", requestId));

        User user = request.getUser();
        return buildUserActivity(user);
    }

    /**
     * Get user activity by user ID
     */
    public UserActivityDto getUserActivityByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return buildUserActivity(user);
    }

    /**
     * Approve a role upgrade request
     */
    @Transactional
    public RoleUpgradeResponseDto approveRequest(Long requestId, Long adminId, String note) {
        log.debug("Approving request {} by admin {}", requestId, adminId);

        RoleUpgradeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request", requestId));

        if (request.getStatus() != RoleUpgradeStatus.PENDING) {
            throw new IllegalStateException("Request has already been processed");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminId));

        User user = request.getUser();

        // Add ORGANIZER role to user
        Role organizerRole = roleRepository.findByName(RoleName.ORGANIZER)
                .orElseThrow(() -> new IllegalStateException("Organizer role not found"));

        user.getRoles().add(organizerRole);
        userRepository.save(user);

        // Update request
        request.setStatus(RoleUpgradeStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setAdminNote(note);
        request.setReviewedAt(LocalDateTime.now());
        requestRepository.save(request);

        log.info("Role upgrade approved for userId: {} by admin: {}", user.getId(), adminId);

        // Notify user
        emailUtil.sendRoleUpgradeApprovalEmail(user.getEmail(), user.getFullName());

        return mapToResponseDto(request);
    }

    /**
     * Reject a role upgrade request
     */
    @Transactional
    public RoleUpgradeResponseDto rejectRequest(Long requestId, Long adminId, String note) {
        log.debug("Rejecting request {} by admin {}", requestId, adminId);

        RoleUpgradeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request", requestId));

        if (request.getStatus() != RoleUpgradeStatus.PENDING) {
            throw new IllegalStateException("Request has already been processed");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminId));

        // Update request
        request.setStatus(RoleUpgradeStatus.REJECTED);
        request.setReviewedBy(admin);
        request.setAdminNote(note);
        request.setReviewedAt(LocalDateTime.now());
        requestRepository.save(request);

        log.info("Role upgrade rejected for userId: {} by admin: {}", request.getUser().getId(), adminId);

        // Notify user
        User user = request.getUser();
        emailUtil.sendRoleUpgradeRejectionEmail(user.getEmail(), user.getFullName(), note);

        return mapToResponseDto(request);
    }

    /**
     * Get count of pending requests (for admin dashboard)
     */
    public long getPendingCount() {
        return requestRepository.countByStatus(RoleUpgradeStatus.PENDING);
    }

    // ==================== HELPER METHODS ====================

    private void notifyAdmins(User requester) {
        // Find all admin users
        Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElse(null);
        if (adminRole == null) return;

        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(adminRole))
                .collect(Collectors.toList());

        for (User admin : admins) {
            try {
                emailUtil.sendRoleUpgradeRequestNotification(
                        admin.getEmail(),
                        admin.getFullName(),
                        requester.getFullName(),
                        requester.getEmail()
                );
            } catch (Exception e) {
                log.error("Failed to notify admin {}: {}", admin.getId(), e.getMessage());
            }
        }
    }

    private UserActivityDto buildUserActivity(User user) {
        UserActivityDto activity = new UserActivityDto();

        // Basic info
        activity.setUserId(user.getId());
        activity.setFullName(user.getFullName());
        activity.setEmail(user.getEmail());
        activity.setProfileImageUrl(user.getProfileImageUrl());
        activity.setMemberSince(user.getCreatedAt());
        activity.setAccountStatus(user.getUserStatus().name());

        // Activity stats
        List<EventEnrollment> enrollments = enrollmentRepository.findByUser_Id(user.getId());
        activity.setEventsAttended(enrollments.size());

        activity.setGroupsJoined(groupMembershipRepository.countByUserAndStatus(user, MembershipStatus.ACTIVE));
        activity.setFollowersCount(userFollowRepository.countByFollowing(user));
        activity.setFollowingCount(userFollowRepository.countByFollower(user));

        // Recent enrollments (last 10)
        List<UserActivityDto.RecentEnrollmentDto> recentEnrollments = enrollments.stream()
                .sorted((a, b) -> b.getEnrolledAt().compareTo(a.getEnrolledAt()))
                .limit(10)
                .map(e -> {
                    UserActivityDto.RecentEnrollmentDto dto = new UserActivityDto.RecentEnrollmentDto();
                    dto.setEventId(e.getEvent().getId());
                    dto.setEventTitle(e.getEvent().getTitle());
                    dto.setEnrolledAt(e.getEnrolledAt());
                    dto.setTicketStatus(e.getTicketStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());

        activity.setRecentEnrollments(recentEnrollments);
        activity.setHasActiveReports(false);  // Could be extended to track user reports

        return activity;
    }

    private RoleUpgradeResponseDto mapToResponseDto(RoleUpgradeRequest request) {
        RoleUpgradeResponseDto dto = new RoleUpgradeResponseDto();
        dto.setId(request.getId());
        dto.setUserId(request.getUser().getId());
        dto.setUserFullName(request.getUser().getFullName());
        dto.setUserEmail(request.getUser().getEmail());
        dto.setUserProfileImageUrl(request.getUser().getProfileImageUrl());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        dto.setAdminNote(request.getAdminNote());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setReviewedAt(request.getReviewedAt());

        if (request.getReviewedBy() != null) {
            dto.setReviewedById(request.getReviewedBy().getId());
            dto.setReviewedByName(request.getReviewedBy().getFullName());
        }

        return dto;
    }
}
