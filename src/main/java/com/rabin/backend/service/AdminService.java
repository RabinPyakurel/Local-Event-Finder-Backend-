package com.rabin.backend.service;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.AdminUserResponseDto;
import com.rabin.backend.dto.response.EventEnrollmentResponseDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.dto.response.GroupMembershipResponseDto;
import com.rabin.backend.dto.response.GroupResponseDto;
import com.rabin.backend.dto.response.PaymentResponseDto;
import com.rabin.backend.dto.response.ReportResponseDto;
import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.enums.MembershipStatus;
import com.rabin.backend.enums.NotificationType;
import com.rabin.backend.enums.PaymentStatus;
import com.rabin.backend.enums.ReportStatus;
import com.rabin.backend.enums.RoleName;
import com.rabin.backend.enums.UserStatus;
import com.rabin.backend.exception.ResourceNotFoundException;
import com.rabin.backend.exception.UserNotFoundException;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventEnrollment;
import com.rabin.backend.model.EventTagMap;
import com.rabin.backend.model.Group;
import com.rabin.backend.model.GroupMembership;
import com.rabin.backend.model.Payment;
import com.rabin.backend.model.Report;
import com.rabin.backend.model.Role;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventEnrollmentRepository;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.EventTagMapRepository;
import com.rabin.backend.repository.GroupMembershipRepository;
import com.rabin.backend.repository.GroupRepository;
import com.rabin.backend.repository.PaymentRepository;
import com.rabin.backend.repository.ReportRepository;
import com.rabin.backend.repository.RoleRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReportRepository reportRepository;
    private final EventTagMapRepository eventTagMapRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final PaymentRepository paymentRepository;
    private final EventEnrollmentRepository eventEnrollmentRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final com.rabin.backend.service.event.EventService eventService;
    private final NotificationService notificationService;
    private final com.rabin.backend.util.EmailUtil emailUtil;

    public AdminService(UserRepository userRepository,
                        EventRepository eventRepository,
                        ReportRepository reportRepository,
                        EventTagMapRepository eventTagMapRepository, GroupRepository groupRepository, GroupMembershipRepository groupMembershipRepository, PaymentRepository paymentRepository, EventEnrollmentRepository eventEnrollmentRepository, RoleRepository roleRepository, ModelMapper modelMapper,
                        com.rabin.backend.service.event.EventService eventService,
                        NotificationService notificationService,
                        com.rabin.backend.util.EmailUtil emailUtil) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reportRepository = reportRepository;
        this.eventTagMapRepository = eventTagMapRepository;
        this.groupRepository = groupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.paymentRepository = paymentRepository;
        this.eventEnrollmentRepository = eventEnrollmentRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.emailUtil = emailUtil;
    }

    // ==================== USER MANAGEMENT ====================

    public GenericApiResponse<List<AdminUserResponseDto>> getAllUsers(Integer page, Integer size) {
        log.debug("Admin: Getting all users, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> usersPage = userRepository.findAll(pageable);
        List<AdminUserResponseDto> users = usersPage.getContent().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} users", users.size());
        return GenericApiResponse.ok(200, "Users retrieved successfully", users);
    }

    @Transactional
    public GenericApiResponse<AdminUserResponseDto> suspendUser(Long userId) {
        log.debug("Admin: Suspending user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getUserStatus() == UserStatus.SUSPENDED) {
            return GenericApiResponse.error(400,"User is already suspended");
        }

        user.setUserStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        // Send suspension email
        try {
            emailUtil.sendEmail(
                    user.getEmail(),
                    "Account Suspended - Local Event Finder",
                    String.format(
                            "Dear %s,\n\n" +
                            "Due to unusual activity detected from your account, your account has been suspended.\n\n" +
                            "While your account is suspended, you will not be able to:\n" +
                            "- Log in to the platform\n" +
                            "- Access any features or services\n\n" +
                            "If you believe this is a mistake or would like to raise an issue, " +
                            "please reply to this email and our team will contact you to resolve the matter.\n\n" +
                            "Best regards,\n" +
                            "Local Event Finder Team",
                            user.getFullName()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send suspension email to user {}: {}", userId, e.getMessage());
        }

        log.info("Admin: User {} suspended successfully", userId);
        return GenericApiResponse.ok(200, "User suspended successfully",
                mapToAdminUserResponse(user));
    }

    @Transactional
    public GenericApiResponse<AdminUserResponseDto> activateUser(Long userId) {
        log.debug("Admin: Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getUserStatus() == UserStatus.ACTIVE) {
            return GenericApiResponse.error(400,"User is already active");
        }

        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Send reactivation email
        try {
            emailUtil.sendEmail(
                    user.getEmail(),
                    "Account Reactivated - Local Event Finder",
                    String.format(
                            "Dear %s,\n\n" +
                            "Good news! Your account has been reviewed and reactivated.\n\n" +
                            "You can now log in and access all features of Local Event Finder as usual.\n\n" +
                            "Thank you for your patience.\n\n" +
                            "Best regards,\n" +
                            "Local Event Finder Team",
                            user.getFullName()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send reactivation email to user {}: {}", userId, e.getMessage());
        }

        log.info("Admin: User {} activated successfully", userId);
        return GenericApiResponse.ok(200, "User activated successfully",
                mapToAdminUserResponse(user));
    }

    public GenericApiResponse<AdminUserResponseDto> getUserById(Long userId) {
        log.debug("Admin: Getting user details for: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return GenericApiResponse.ok(200, "User retrieved successfully",
                mapToAdminUserResponse(user));
    }

    // ==================== EVENT MODERATION ====================

    public GenericApiResponse<List<EventResponseDto>> getAllEvents(Integer page, Integer size) {
        log.debug("Admin: Getting all events, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Event> eventsPage = eventRepository.findAll(pageable);
        List<EventResponseDto> events = eventsPage.getContent().stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} events", events.size());
        return GenericApiResponse.ok(200, "Events retrieved successfully", events);
    }

    @Transactional
    public GenericApiResponse<Void> removeEvent(Long eventId) {
        log.debug("Admin: Removing event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        event.setEventStatus(EventStatus.INACTIVE);
        eventRepository.save(event);

        log.info("Admin: Event {} removed successfully", eventId);
        return GenericApiResponse.ok(200, "Event removed successfully", null);
    }

    @Transactional
    public GenericApiResponse<Void> deleteEvent(Long eventId) {
        log.debug("Admin: Permanently deleting event: {}", eventId);

        if (!eventRepository.existsById(eventId)) {
            return GenericApiResponse.error(404,"Event not found");
        }

        eventService.deleteEventById(eventId);

        log.info("Admin: Event {} deleted permanently", eventId);
        return GenericApiResponse.ok(200, "Event deleted permanently", null);
    }

    // ==================== REPORT HANDLING ====================

    public GenericApiResponse<List<ReportResponseDto>> getAllReports() {
        log.debug("Admin: Getting all reports");

        List<Report> reports = reportRepository.findAll();
        List<ReportResponseDto> reportDtos = reports.stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} reports", reportDtos.size());
        return GenericApiResponse.ok(200, "Reports retrieved successfully", reportDtos);
    }

    public GenericApiResponse<List<ReportResponseDto>> getPendingReports() {
        log.debug("Admin: Getting pending reports");

        List<Report> reports = reportRepository.findByReportStatus(ReportStatus.PENDING);
        List<ReportResponseDto> reportDtos = reports.stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} pending reports", reportDtos.size());
        return GenericApiResponse.ok(200, "Pending reports retrieved successfully", reportDtos);
    }

    @Transactional
    public GenericApiResponse<ReportResponseDto> resolveReport(Long reportId) {
        log.debug("Admin: Resolving report: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setReportStatus(ReportStatus.RESOLVED);
        reportRepository.save(report);

        // Notify the reporter that their report was resolved
        notificationService.sendNotification(
                report.getReporter().getId(),
                NotificationType.REPORT_RESOLVED,
                "Report Resolved",
                "Your report on event '" + report.getEvent().getTitle() + "' has been reviewed and action has been taken. Thank you for helping keep our community safe.",
                report.getEvent().getId(),
                "EVENT"
        );

        log.info("Admin: Report {} resolved successfully", reportId);
        return GenericApiResponse.ok(200, "Report resolved successfully",
                mapToReportResponse(report));
    }

    @Transactional
    public GenericApiResponse<ReportResponseDto> rejectReport(Long reportId) {
        log.debug("Admin: Rejecting report: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setReportStatus(ReportStatus.REJECTED);
        reportRepository.save(report);

        // Notify the reporter that their report was reviewed but no action taken
        notificationService.sendNotification(
                report.getReporter().getId(),
                NotificationType.REPORT_RESOLVED,
                "Report Reviewed",
                "Your report on event '" + report.getEvent().getTitle() + "' has been reviewed. After investigation, no violation was found.",
                report.getEvent().getId(),
                "EVENT"
        );

        log.info("Admin: Report {} rejected successfully", reportId);
        return GenericApiResponse.ok(200, "Report rejected successfully",
                mapToReportResponse(report));
    }

    // ==================== HELPER METHODS ====================

    private AdminUserResponseDto mapToAdminUserResponse(User user) {
        AdminUserResponseDto dto = new AdminUserResponseDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setDob(user.getDob());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setUserStatus(user.getUserStatus().name());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        dto.setRoles(roles);

        return dto;
    }

    private Long getCurrentUserIdOrNull() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
                return userDetails.getId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private EventResponseDto mapToEventResponse(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setVenue(event.getVenue());
        dto.setEventImageUrl(event.getEventImageUrl());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setOrganizerName(event.getCreatedBy().getFullName());
        dto.setEventStatus(event.getEventStatus().name());
        dto.setOrganizerId(event.getCreatedBy().getId());
        dto.setOrganizerProfileImage(event.getCreatedBy().getProfileImageUrl());

        List<EventTagMap> tagMaps = eventTagMapRepository.findByEvent(event);
        List<String> tags = tagMaps.stream()
                .map(tm -> tm.getEventTag().getTagKey())
                .collect(Collectors.toList());
        dto.setTags(tags);

        Long currentUserId = getCurrentUserIdOrNull();
        dto.setIsEventOwner(currentUserId != null && currentUserId.equals(event.getCreatedBy().getId()));

        return dto;
    }

    private ReportResponseDto mapToReportResponse(Report report) {
        ReportResponseDto dto = new ReportResponseDto();
        dto.setId(report.getId());
        dto.setReporterId(report.getReporter().getId());
        dto.setReporterName(report.getReporter().getFullName());
        dto.setReporterEmail(report.getReporter().getEmail());
        dto.setEventId(report.getEvent().getId());
        dto.setEventTitle(report.getEvent().getTitle());
        dto.setReason(report.getReason());
        dto.setReportStatus(report.getReportStatus().name());
        dto.setCreatedAt(report.getCreatedAt());

        return dto;
    }

    public GenericApiResponse<Map<String, Object>> getDashboardStats() {
        log.debug("Admin: Getting dashboard statistics");

        Map<String, Object> stats = new HashMap<>();

        // User statistics
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByUserStatus(UserStatus.ACTIVE);
        long suspendedUsers = userRepository.countByUserStatus(UserStatus.SUSPENDED);

        Map<String, Long> userStats = new HashMap<>();
        userStats.put("total", totalUsers);
        userStats.put("active", activeUsers);
        userStats.put("suspended", suspendedUsers);
        stats.put("users", userStats);

        // Event statistics
        long totalEvents = eventRepository.count();
        long activeEvents = eventRepository.countByEventStatus(EventStatus.ACTIVE);
        long inactiveEvents = eventRepository.countByEventStatus(EventStatus.INACTIVE);

        Map<String, Long> eventStats = new HashMap<>();
        eventStats.put("total", totalEvents);
        eventStats.put("active", activeEvents);
        eventStats.put("inactive", inactiveEvents);
        stats.put("events", eventStats);

        // Enrollment statistics
        long totalEnrollments = eventEnrollmentRepository.count();
        stats.put("totalEnrollments", totalEnrollments);

        // Group statistics
        long totalGroups = groupRepository.count();
        long activeGroups = groupRepository.countByIsActive(true);
        stats.put("totalGroups", totalGroups);
        stats.put("activeGroups", activeGroups);

        // Payment statistics
        long totalPayments = paymentRepository.count();
        long completedPayments = paymentRepository.countByPaymentStatus(PaymentStatus.COMPLETED);
        long pendingPayments = paymentRepository.countByPaymentStatus(PaymentStatus.PENDING);

        Map<String, Long> paymentStats = new HashMap<>();
        paymentStats.put("total", totalPayments);
        paymentStats.put("completed", completedPayments);
        paymentStats.put("pending", pendingPayments);
        stats.put("payments", paymentStats);

        // Report statistics
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByReportStatus(ReportStatus.PENDING);

        Map<String, Long> reportStats = new HashMap<>();
        reportStats.put("total", totalReports);
        reportStats.put("pending", pendingReports);
        stats.put("reports", reportStats);

        log.info("Admin: Dashboard stats retrieved successfully");
        return GenericApiResponse.ok(200, "Dashboard statistics retrieved successfully", stats);
    }

    public GenericApiResponse<List<GroupResponseDto>> getAllGroups(Integer page, Integer size) {
        log.debug("Admin: Getting all groups, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Group> groupsPage = groupRepository.findAll(pageable);
        List<GroupResponseDto> groups = groupsPage.getContent().stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} groups", groups.size());
        return GenericApiResponse.ok(200, "Groups retrieved successfully", groups);
    }

    @Transactional
    public GenericApiResponse<Void> deleteGroup(Long groupId) {
        log.debug("Admin: Deleting group: {}", groupId);

        if (!groupRepository.existsById(groupId)) {
            return GenericApiResponse.error(404, "Group not found");
        }

        groupRepository.deleteById(groupId);

        log.info("Admin: Group {} deleted successfully", groupId);
        return GenericApiResponse.ok(200, "Group deleted successfully", null);
    }

    public GenericApiResponse<List<GroupMembershipResponseDto>> getGroupMemberships(Long groupId) {
        log.debug("Admin: Getting memberships for group: {}", groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));

        List<GroupMembership> memberships = groupMembershipRepository.findByGroup(group);
        List<GroupMembershipResponseDto> membershipDtos = memberships.stream()
                .map(this::mapToGroupMembershipResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} memberships for group {}", membershipDtos.size(), groupId);
        return GenericApiResponse.ok(200, "Group memberships retrieved successfully", membershipDtos);
    }

    @Transactional
    public GenericApiResponse<GroupMembershipResponseDto> approveMembership(Long membershipId) {
        log.debug("Admin: Approving membership: {}", membershipId);

        GroupMembership membership = groupMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", membershipId));

        membership.setStatus(MembershipStatus.ACTIVE);
        groupMembershipRepository.save(membership);

        log.info("Admin: Membership {} approved successfully", membershipId);
        return GenericApiResponse.ok(200, "Membership approved successfully",
                mapToGroupMembershipResponse(membership));
    }

    @Transactional
    public GenericApiResponse<Void> rejectMembership(Long membershipId) {
        log.debug("Admin: Rejecting membership: {}", membershipId);

        if (!groupMembershipRepository.existsById(membershipId)) {
            return GenericApiResponse.error(404, "Membership not found");
        }

        groupMembershipRepository.deleteById(membershipId);

        log.info("Admin: Membership {} rejected successfully", membershipId);
        return GenericApiResponse.ok(200, "Membership rejected successfully", null);
    }

    @Transactional
    public GenericApiResponse<GroupMembershipResponseDto> banMember(Long membershipId) {
        log.debug("Admin: Banning member: {}", membershipId);

        GroupMembership membership = groupMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", membershipId));

        membership.setStatus(MembershipStatus.BANNED);
        groupMembershipRepository.save(membership);

        log.info("Admin: Member {} banned successfully", membershipId);
        return GenericApiResponse.ok(200, "Member banned successfully",
                mapToGroupMembershipResponse(membership));
    }

    // ==================== PAYMENT MANAGEMENT ====================

    public GenericApiResponse<List<PaymentResponseDto>> getAllPayments(Integer page, Integer size) {
        log.debug("Admin: Getting all payments, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Payment> paymentsPage = paymentRepository.findAll(pageable);
        List<PaymentResponseDto> payments = paymentsPage.getContent().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} payments", payments.size());
        return GenericApiResponse.ok(200, "Payments retrieved successfully", payments);
    }

    public GenericApiResponse<Map<String, Object>> getPaymentStats() {
        log.debug("Admin: Getting payment statistics");

        Map<String, Object> stats = new HashMap<>();

        long totalPayments = paymentRepository.count();
        long completedPayments = paymentRepository.countByPaymentStatus(PaymentStatus.COMPLETED);
        long pendingPayments = paymentRepository.countByPaymentStatus(PaymentStatus.PENDING);
        long failedPayments = paymentRepository.countByPaymentStatus(PaymentStatus.FAILED);

        stats.put("total", totalPayments);
        stats.put("completed", completedPayments);
        stats.put("pending", pendingPayments);
        stats.put("failed", failedPayments);

        // Calculate total revenue from completed payments
        List<Payment> allPayments = paymentRepository.findAll();
        double totalRevenue = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
        stats.put("totalRevenue", totalRevenue);

        log.info("Admin: Payment stats retrieved successfully");
        return GenericApiResponse.ok(200, "Payment statistics retrieved successfully", stats);
    }

    // ==================== REFUND MANAGEMENT ====================

    public GenericApiResponse<List<PaymentResponseDto>> getPendingRefunds() {
        log.debug("Admin: Getting pending refunds");

        // Find payments with REFUNDED status but not yet processed
        List<Payment> pendingRefunds = paymentRepository.findByPaymentStatusAndRefundProcessed(
                PaymentStatus.REFUNDED, false);

        List<PaymentResponseDto> refunds = pendingRefunds.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} pending refunds", refunds.size());
        return GenericApiResponse.ok(200, "Pending refunds retrieved successfully", refunds);
    }

    @Transactional
    public GenericApiResponse<PaymentResponseDto> processRefund(Long paymentId, String note) {
        log.debug("Admin: Processing refund for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.REFUNDED) {
            return GenericApiResponse.error(400, "Payment is not marked for refund");
        }

        if (Boolean.TRUE.equals(payment.getRefundProcessed())) {
            return GenericApiResponse.error(400, "Refund has already been processed");
        }

        // Mark refund as processed
        payment.setRefundProcessed(true);
        payment.setRefundedAt(java.time.LocalDateTime.now());
        if (note != null && !note.trim().isEmpty()) {
            payment.setRefundNote(note);
        }

        paymentRepository.save(payment);

        log.info("Admin: Refund processed for payment: {}, amount: {}", paymentId, payment.getAmount());
        return GenericApiResponse.ok(200, "Refund processed successfully", mapToPaymentResponse(payment));
    }

    public GenericApiResponse<Map<String, Object>> getRefundStats() {
        log.debug("Admin: Getting refund statistics");

        Map<String, Object> stats = new HashMap<>();

        // Count total refunds
        long totalRefunds = paymentRepository.countByPaymentStatus(PaymentStatus.REFUNDED);
        long pendingRefunds = paymentRepository.countByPaymentStatusAndRefundProcessed(PaymentStatus.REFUNDED, false);
        long processedRefunds = paymentRepository.countByPaymentStatusAndRefundProcessed(PaymentStatus.REFUNDED, true);

        stats.put("total", totalRefunds);
        stats.put("pending", pendingRefunds);
        stats.put("processed", processedRefunds);

        // Calculate total refund amount
        List<Payment> allRefunds = paymentRepository.findByPaymentStatusAndRefundProcessed(PaymentStatus.REFUNDED, false);
        double pendingRefundAmount = allRefunds.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
        stats.put("pendingAmount", pendingRefundAmount);

        log.info("Admin: Refund stats retrieved successfully");
        return GenericApiResponse.ok(200, "Refund statistics retrieved successfully", stats);
    }

    // ==================== ENROLLMENT MANAGEMENT ====================

    public GenericApiResponse<List<EventEnrollmentResponseDto>> getAllEnrollments(Integer page, Integer size) {
        log.debug("Admin: Getting all enrollments, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "enrolledAt")
        );

        Page<EventEnrollment> enrollmentsPage = eventEnrollmentRepository.findAll(pageable);
        List<EventEnrollmentResponseDto> enrollments = enrollmentsPage.getContent().stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} enrollments", enrollments.size());
        return GenericApiResponse.ok(200, "Enrollments retrieved successfully", enrollments);
    }

    // ==================== ROLE MANAGEMENT ====================

    @Transactional
    public GenericApiResponse<AdminUserResponseDto> assignRole(Long userId, String roleName) {
        log.debug("Admin: Assigning role {} to user {}", roleName, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        try {
            RoleName roleNameEnum = RoleName.valueOf(roleName.toUpperCase());
            Role role = roleRepository.findByName(roleNameEnum)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

            if (user.getRoles().contains(role)) {
                return GenericApiResponse.error(400, "User already has this role");
            }

            user.getRoles().add(role);
            userRepository.save(user);

            log.info("Admin: Role {} assigned to user {} successfully", roleName, userId);
            return GenericApiResponse.ok(200, "Role assigned successfully",
                    mapToAdminUserResponse(user));
        } catch (IllegalArgumentException e) {
            return GenericApiResponse.error(400, "Invalid role name: " + roleName);
        }
    }

    @Transactional
    public GenericApiResponse<AdminUserResponseDto> removeRole(Long userId, String roleName) {
        log.debug("Admin: Removing role {} from user {}", roleName, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        try {
            RoleName roleNameEnum = RoleName.valueOf(roleName.toUpperCase());
            Role role = roleRepository.findByName(roleNameEnum)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));

            if (!user.getRoles().contains(role)) {
                return GenericApiResponse.error(400, "User does not have this role");
            }

            user.getRoles().remove(role);
            userRepository.save(user);

            log.info("Admin: Role {} removed from user {} successfully", roleName, userId);
            return GenericApiResponse.ok(200, "Role removed successfully",
                    mapToAdminUserResponse(user));
        } catch (IllegalArgumentException e) {
            return GenericApiResponse.error(400, "Invalid role name: " + roleName);
        }
    }

    private GroupResponseDto mapToGroupResponse(Group group) {
        GroupResponseDto dto = new GroupResponseDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setGroupImageUrl(group.getGroupImageUrl());
        dto.setCreatorId(group.getCreatedBy().getId());
        dto.setCreatorName(group.getCreatedBy().getFullName());
        dto.setRequiresApproval(group.getRequiresApproval());
        dto.setIsActive(group.getIsActive());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        return dto;
    }

    private GroupMembershipResponseDto mapToGroupMembershipResponse(GroupMembership membership) {
        GroupMembershipResponseDto dto = new GroupMembershipResponseDto();
        dto.setId(membership.getId());
        dto.setUserId(membership.getUser().getId());
        dto.setUserFullName(membership.getUser().getFullName());
        dto.setUserEmail(membership.getUser().getEmail());
        dto.setUserProfileImageUrl(membership.getUser().getProfileImageUrl());
        dto.setGroupId(membership.getGroup().getId());
        dto.setGroupName(membership.getGroup().getName());
        dto.setStatus(membership.getStatus());
        dto.setIsAdmin(membership.getIsAdmin());
        dto.setJoinedAt(membership.getJoinedAt());
        return dto;
    }

    private PaymentResponseDto mapToPaymentResponse(Payment payment) {
        PaymentResponseDto dto = modelMapper.map(payment, PaymentResponseDto.class);
        dto.setUserId(payment.getUser().getId());
        dto.setUserName(payment.getUser().getFullName());
        dto.setUserEmail(payment.getUser().getEmail());
        dto.setEventId(payment.getEvent().getId());
        dto.setEventTitle(payment.getEvent().getTitle());
        dto.setRefundProcessed(payment.getRefundProcessed());
        dto.setRefundedAt(payment.getRefundedAt());
        dto.setRefundNote(payment.getRefundNote());
        return dto;
    }

    private EventEnrollmentResponseDto mapToEnrollmentResponse(EventEnrollment enrollment) {
        EventEnrollmentResponseDto dto = modelMapper.map(enrollment, EventEnrollmentResponseDto.class);
        dto.setEnrollmentId(enrollment.getId());
        dto.setEventId(enrollment.getEvent().getId());
        dto.setEventTitle(enrollment.getEvent().getTitle());
        dto.setStartDate(enrollment.getEvent().getStartDate());
        dto.setEndDate(enrollment.getEvent().getEndDate());
        dto.setVenue(enrollment.getEvent().getVenue());
        dto.setUserId(enrollment.getUser().getId());
        dto.setUserFullName(enrollment.getUser().getFullName());
        dto.setUserEmail(enrollment.getUser().getEmail());
        return dto;
    }

    // ==================== ENHANCED ANALYTICS ====================

    public GenericApiResponse<Map<String, Object>> getComprehensiveAnalytics() {
        log.debug("Admin: Getting comprehensive analytics");

        Map<String, Object> analytics = new HashMap<>();

        // Event statistics by status
        Map<String, Long> eventsByStatus = new HashMap<>();
        eventsByStatus.put("active", eventRepository.countByEventStatus(EventStatus.ACTIVE));
        eventsByStatus.put("inactive", eventRepository.countByEventStatus(EventStatus.INACTIVE));
        eventsByStatus.put("cancelled", eventRepository.countByEventStatus(EventStatus.CANCELLED));
        eventsByStatus.put("completed", eventRepository.countByEventStatus(EventStatus.COMPLETED));
        eventsByStatus.put("total", eventRepository.count());
        analytics.put("eventsByStatus", eventsByStatus);

        // Paid vs free events
        List<Event> allEvents = eventRepository.findAll();
        long paidEvents = allEvents.stream().filter(e -> Boolean.TRUE.equals(e.getIsPaid())).count();
        long freeEvents = allEvents.size() - paidEvents;
        Map<String, Long> eventsByType = new HashMap<>();
        eventsByType.put("paid", paidEvents);
        eventsByType.put("free", freeEvents);
        analytics.put("eventsByType", eventsByType);

        // Revenue analytics
        List<Payment> completedPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());

        double totalRevenue = completedPayments.stream().mapToDouble(Payment::getAmount).sum();
        double refundedAmount = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.REFUNDED)
                .mapToDouble(Payment::getAmount).sum();
        double netRevenue = Math.max(0, totalRevenue - refundedAmount);

        Map<String, Object> revenueStats = new HashMap<>();
        revenueStats.put("totalRevenue", totalRevenue);
        revenueStats.put("refundedAmount", refundedAmount);
        revenueStats.put("netRevenue", netRevenue);
        revenueStats.put("totalTransactions", completedPayments.size());
        analytics.put("revenue", revenueStats);

        // Top organizers (by event count)
        Map<Long, Long> organizerEventCount = allEvents.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCreatedBy().getId(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> topOrganizers = organizerEventCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(entry -> {
                    User organizer = userRepository.findById(entry.getKey()).orElse(null);
                    if (organizer == null) return null;

                    // Calculate revenue for this organizer
                    double organizerRevenue = completedPayments.stream()
                            .filter(p -> p.getEvent().getCreatedBy().getId().equals(entry.getKey()))
                            .mapToDouble(Payment::getAmount).sum();

                    long enrollments = allEvents.stream()
                            .filter(e -> e.getCreatedBy().getId().equals(entry.getKey()))
                            .mapToInt(e -> e.getBookedSeats() != null ? e.getBookedSeats() : 0)
                            .sum();

                    Map<String, Object> orgData = new HashMap<>();
                    orgData.put("id", organizer.getId());
                    orgData.put("fullName", organizer.getFullName());
                    orgData.put("email", organizer.getEmail());
                    orgData.put("profileImageUrl", organizer.getProfileImageUrl());
                    orgData.put("eventCount", entry.getValue());
                    orgData.put("totalEnrollments", enrollments);
                    orgData.put("totalRevenue", organizerRevenue);
                    return orgData;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        analytics.put("topOrganizers", topOrganizers);

        // Ticket stats
        long totalTickets = eventEnrollmentRepository.count();
        Map<String, Long> ticketStats = new HashMap<>();
        ticketStats.put("total", totalTickets);
        analytics.put("tickets", ticketStats);

        log.info("Admin: Comprehensive analytics retrieved successfully");
        return GenericApiResponse.ok(200, "Comprehensive analytics retrieved successfully", analytics);
    }

}
