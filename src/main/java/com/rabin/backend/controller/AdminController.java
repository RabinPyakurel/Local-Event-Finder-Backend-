package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.AdminUserResponseDto;
import com.rabin.backend.dto.response.EventEnrollmentResponseDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.dto.response.GroupMembershipResponseDto;
import com.rabin.backend.dto.response.GroupResponseDto;
import com.rabin.backend.dto.response.PaymentResponseDto;
import com.rabin.backend.dto.response.ReportResponseDto;
import com.rabin.backend.dto.response.RoleUpgradeResponseDto;
import com.rabin.backend.dto.response.UserActivityDto;
import com.rabin.backend.enums.RoleUpgradeStatus;
import com.rabin.backend.service.AdminService;
import com.rabin.backend.service.RoleUpgradeService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only APIs for user management, event moderation, reports, groups, payments, and role upgrades")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final RoleUpgradeService roleUpgradeService;

    public AdminController(AdminService adminService, RoleUpgradeService roleUpgradeService) {
        this.adminService = adminService;
        this.roleUpgradeService = roleUpgradeService;
    }

    // ==================== USER MANAGEMENT ====================

    @Operation(summary = "Get all users", description = "Get paginated list of all users in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @GetMapping("/users")
    public ResponseEntity<GenericApiResponse<List<AdminUserResponseDto>>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all users request, page: {}, size: {}", page, size);
        GenericApiResponse<List<AdminUserResponseDto>> response = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user by ID", description = "Get detailed user information by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.debug("Admin: Get user by ID request for: {}", userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Suspend user", description = "Suspend a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User suspended successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> suspendUser(
            @Parameter(description = "User ID to suspend") @PathVariable Long userId) {
        log.debug("Admin: Suspend user request for: {}", userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.suspendUser(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate user", description = "Activate a suspended user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> activateUser(
            @Parameter(description = "User ID to activate") @PathVariable Long userId) {
        log.debug("Admin: Activate user request for: {}", userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.activateUser(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user activity", description = "Get activity statistics for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activity retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}/activity")
    public ResponseEntity<GenericApiResponse<UserActivityDto>> getUserActivity(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.debug("Admin: Get user activity for userId: {}", userId);
        UserActivityDto activity = roleUpgradeService.getUserActivityByUserId(userId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "User activity retrieved successfully", activity)
        );
    }

    @Operation(summary = "Assign role to user", description = "Assign a role to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role name"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/users/{userId}/assign-role")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> assignRole(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Role name (USER, ORGANIZER, ADMIN)") @RequestParam String roleName
    ) {
        log.debug("Admin: Assign role {} to user {}", roleName, userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.assignRole(userId, roleName);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove role from user", description = "Remove a role from a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role name"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/users/{userId}/remove-role")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> removeRole(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Role name (USER, ORGANIZER, ADMIN)") @RequestParam String roleName
    ) {
        log.debug("Admin: Remove role {} from user {}", roleName, userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.removeRole(userId, roleName);
        return ResponseEntity.ok(response);
    }

    // ==================== EVENT MODERATION ====================

    @Operation(summary = "Get all events", description = "Get paginated list of all events for moderation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
    })
    @GetMapping("/events")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getAllEvents(
            @Parameter(description = "Page number") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all events request, page: {}, size: {}", page, size);
        GenericApiResponse<List<EventResponseDto>> response = adminService.getAllEvents(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove event", description = "Soft remove an event (mark as cancelled)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event removed successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PostMapping("/events/{eventId}/remove")
    public ResponseEntity<GenericApiResponse<Void>> removeEvent(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        log.debug("Admin: Remove event request for: {}", eventId);
        GenericApiResponse<Void> response = adminService.removeEvent(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete event", description = "Permanently delete an event from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<GenericApiResponse<Void>> deleteEvent(
            @Parameter(description = "Event ID") @PathVariable Long eventId) {
        log.debug("Admin: Delete event permanently request for: {}", eventId);
        GenericApiResponse<Void> response = adminService.deleteEvent(eventId);
        return ResponseEntity.ok(response);
    }

    // ==================== REPORT HANDLING ====================

    @Operation(summary = "Get all reports", description = "Get all user reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    })
    @GetMapping("/reports")
    public ResponseEntity<GenericApiResponse<List<ReportResponseDto>>> getAllReports() {
        log.debug("Admin: Get all reports request");
        GenericApiResponse<List<ReportResponseDto>> response = adminService.getAllReports();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get pending reports", description = "Get all pending reports awaiting review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending reports retrieved successfully")
    })
    @GetMapping("/reports/pending")
    public ResponseEntity<GenericApiResponse<List<ReportResponseDto>>> getPendingReports() {
        log.debug("Admin: Get pending reports request");
        GenericApiResponse<List<ReportResponseDto>> response = adminService.getPendingReports();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resolve report", description = "Mark a report as resolved")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report resolved successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<GenericApiResponse<ReportResponseDto>> resolveReport(
            @Parameter(description = "Report ID") @PathVariable Long reportId) {
        log.debug("Admin: Resolve report request for: {}", reportId);
        GenericApiResponse<ReportResponseDto> response = adminService.resolveReport(reportId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reject report", description = "Reject a report as invalid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report rejected successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @PutMapping("/reports/{reportId}/reject")
    public ResponseEntity<GenericApiResponse<ReportResponseDto>> rejectReport(
            @Parameter(description = "Report ID") @PathVariable Long reportId) {
        log.debug("Admin: Reject report request for: {}", reportId);
        GenericApiResponse<ReportResponseDto> response = adminService.rejectReport(reportId);
        return ResponseEntity.ok(response);
    }

    // ==================== DASHBOARD & ANALYTICS ====================

    @Operation(summary = "Get dashboard stats", description = "Get dashboard statistics overview")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard stats retrieved successfully")
    })
    @GetMapping("/dashboard/stats")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> getDashboardStats() {
        log.debug("Admin: Get dashboard statistics request");
        GenericApiResponse<Map<String, Object>> response = adminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get comprehensive analytics", description = "Get detailed analytics data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully")
    })
    @GetMapping("/analytics")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> getComprehensiveAnalytics() {
        log.debug("Admin: Get comprehensive analytics request");
        GenericApiResponse<Map<String, Object>> response = adminService.getComprehensiveAnalytics();
        return ResponseEntity.ok(response);
    }

    // ==================== GROUP MANAGEMENT ====================

    @Operation(summary = "Get all groups", description = "Get paginated list of all groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    @GetMapping("/groups")
    public ResponseEntity<GenericApiResponse<List<GroupResponseDto>>> getAllGroups(
            @Parameter(description = "Page number") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all groups request, page: {}, size: {}", page, size);
        GenericApiResponse<List<GroupResponseDto>> response = adminService.getAllGroups(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete group", description = "Permanently delete a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<GenericApiResponse<Void>> deleteGroup(
            @Parameter(description = "Group ID") @PathVariable Long groupId) {
        log.debug("Admin: Delete group request for: {}", groupId);
        GenericApiResponse<Void> response = adminService.deleteGroup(groupId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get group memberships", description = "Get all memberships for a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Memberships retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/groups/{groupId}/memberships")
    public ResponseEntity<GenericApiResponse<List<GroupMembershipResponseDto>>> getGroupMemberships(
            @Parameter(description = "Group ID") @PathVariable Long groupId
    ) {
        log.debug("Admin: Get group memberships request for group: {}", groupId);
        GenericApiResponse<List<GroupMembershipResponseDto>> response = adminService.getGroupMemberships(groupId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Approve membership", description = "Approve a pending group membership request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membership approved successfully"),
            @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PostMapping("/groups/memberships/{membershipId}/approve")
    public ResponseEntity<GenericApiResponse<GroupMembershipResponseDto>> approveMembership(
            @Parameter(description = "Membership ID") @PathVariable Long membershipId
    ) {
        log.debug("Admin: Approve membership request for: {}", membershipId);
        GenericApiResponse<GroupMembershipResponseDto> response = adminService.approveMembership(membershipId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reject membership", description = "Reject a pending group membership request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membership rejected successfully"),
            @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PostMapping("/groups/memberships/{membershipId}/reject")
    public ResponseEntity<GenericApiResponse<Void>> rejectMembership(
            @Parameter(description = "Membership ID") @PathVariable Long membershipId) {
        log.debug("Admin: Reject membership request for: {}", membershipId);
        GenericApiResponse<Void> response = adminService.rejectMembership(membershipId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ban member", description = "Ban a member from a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member banned successfully"),
            @ApiResponse(responseCode = "404", description = "Membership not found")
    })
    @PostMapping("/groups/memberships/{membershipId}/ban")
    public ResponseEntity<GenericApiResponse<GroupMembershipResponseDto>> banMember(
            @Parameter(description = "Membership ID") @PathVariable Long membershipId) {
        log.debug("Admin: Ban member request for: {}", membershipId);
        GenericApiResponse<GroupMembershipResponseDto> response = adminService.banMember(membershipId);
        return ResponseEntity.ok(response);
    }

    // ==================== PAYMENT MANAGEMENT ====================

    @Operation(summary = "Get all payments", description = "Get paginated list of all payments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
    })
    @GetMapping("/payments")
    public ResponseEntity<GenericApiResponse<List<PaymentResponseDto>>> getAllPayments(
            @Parameter(description = "Page number") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all payments request, page: {}, size: {}", page, size);
        GenericApiResponse<List<PaymentResponseDto>> response = adminService.getAllPayments(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payment stats", description = "Get payment statistics overview")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment stats retrieved successfully")
    })
    @GetMapping("/payments/stats")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> getPaymentStats() {
        log.debug("Admin: Get payment statistics request");
        GenericApiResponse<Map<String, Object>> response = adminService.getPaymentStats();
        return ResponseEntity.ok(response);
    }

    // ==================== REFUND MANAGEMENT ====================

    @Operation(summary = "Get pending refunds", description = "Get all pending refund requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending refunds retrieved successfully")
    })
    @GetMapping("/refunds/pending")
    public ResponseEntity<GenericApiResponse<List<PaymentResponseDto>>> getPendingRefunds() {
        log.debug("Admin: Get pending refunds request");
        GenericApiResponse<List<PaymentResponseDto>> response = adminService.getPendingRefunds();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Process refund", description = "Process a pending refund request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/refunds/{paymentId}/process")
    public ResponseEntity<GenericApiResponse<PaymentResponseDto>> processRefund(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId,
            @Parameter(description = "Admin note") @RequestParam(required = false) String note
    ) {
        log.debug("Admin: Process refund for payment: {}", paymentId);
        GenericApiResponse<PaymentResponseDto> response = adminService.processRefund(paymentId, note);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get refund stats", description = "Get refund statistics overview")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund stats retrieved successfully")
    })
    @GetMapping("/refunds/stats")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> getRefundStats() {
        log.debug("Admin: Get refund statistics request");
        GenericApiResponse<Map<String, Object>> response = adminService.getRefundStats();
        return ResponseEntity.ok(response);
    }

    // ==================== ENROLLMENT MANAGEMENT ====================

    @Operation(summary = "Get all enrollments", description = "Get paginated list of all event enrollments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully")
    })
    @GetMapping("/enrollments")
    public ResponseEntity<GenericApiResponse<List<EventEnrollmentResponseDto>>> getAllEnrollments(
            @Parameter(description = "Page number") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all enrollments request, page: {}, size: {}", page, size);
        GenericApiResponse<List<EventEnrollmentResponseDto>> response = adminService.getAllEnrollments(page, size);
        return ResponseEntity.ok(response);
    }

    // ==================== ROLE UPGRADE REQUESTS ====================

    @Operation(summary = "Get role upgrade requests", description = "Get all role upgrade requests with optional status filter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role upgrade requests retrieved successfully")
    })
    @GetMapping("/role-upgrades")
    public ResponseEntity<GenericApiResponse<List<RoleUpgradeResponseDto>>> getRoleUpgradeRequests(
            @Parameter(description = "Filter by status") @RequestParam(required = false) RoleUpgradeStatus status,
            @Parameter(description = "Page number") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get role upgrade requests, status: {}", status);
        List<RoleUpgradeResponseDto> requests = roleUpgradeService.getAllRequests(status, page, size);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Role upgrade requests retrieved successfully", requests)
        );
    }

    @Operation(summary = "Get pending upgrade count", description = "Get count of pending role upgrade requests for notification badge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @GetMapping("/role-upgrades/pending/count")
    public ResponseEntity<GenericApiResponse<Long>> getPendingRoleUpgradeCount() {
        log.debug("Admin: Get pending role upgrade count");
        long count = roleUpgradeService.getPendingCount();
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Count retrieved successfully", count)
        );
    }

    @Operation(summary = "Get user activity for request", description = "Get user activity statistics for a role upgrade request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activity retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @GetMapping("/role-upgrades/{requestId}/activity")
    public ResponseEntity<GenericApiResponse<UserActivityDto>> getUserActivityForRequest(
            @Parameter(description = "Role upgrade request ID") @PathVariable Long requestId
    ) {
        log.debug("Admin: Get user activity for request: {}", requestId);
        UserActivityDto activity = roleUpgradeService.getUserActivity(requestId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "User activity retrieved successfully", activity)
        );
    }

    @Operation(summary = "Approve role upgrade", description = "Approve a role upgrade request to make user an organizer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role upgrade approved successfully"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PostMapping("/role-upgrades/{requestId}/approve")
    public ResponseEntity<GenericApiResponse<RoleUpgradeResponseDto>> approveRoleUpgrade(
            @Parameter(description = "Role upgrade request ID") @PathVariable Long requestId,
            @Parameter(description = "Admin note") @RequestParam(required = false) String note
    ) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.debug("Admin: Approve role upgrade request: {} by admin: {}", requestId, adminId);

        RoleUpgradeResponseDto response = roleUpgradeService.approveRequest(requestId, adminId, note);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Role upgrade approved successfully", response)
        );
    }

    @Operation(summary = "Reject role upgrade", description = "Reject a role upgrade request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role upgrade rejected"),
            @ApiResponse(responseCode = "404", description = "Request not found")
    })
    @PostMapping("/role-upgrades/{requestId}/reject")
    public ResponseEntity<GenericApiResponse<RoleUpgradeResponseDto>> rejectRoleUpgrade(
            @Parameter(description = "Role upgrade request ID") @PathVariable Long requestId,
            @Parameter(description = "Rejection reason") @RequestParam(required = false) String note
    ) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.debug("Admin: Reject role upgrade request: {} by admin: {}", requestId, adminId);

        RoleUpgradeResponseDto response = roleUpgradeService.rejectRequest(requestId, adminId, note);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Role upgrade rejected", response)
        );
    }
}
