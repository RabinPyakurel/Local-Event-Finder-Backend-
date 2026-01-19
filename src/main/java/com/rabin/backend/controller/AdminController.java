package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.AdminUserResponseDto;
import com.rabin.backend.dto.response.EventEnrollmentResponseDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.dto.response.GroupMembershipResponseDto;
import com.rabin.backend.dto.response.GroupResponseDto;
import com.rabin.backend.dto.response.PaymentResponseDto;
import com.rabin.backend.dto.response.ReportResponseDto;
import com.rabin.backend.service.AdminService;
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
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public ResponseEntity<GenericApiResponse<List<AdminUserResponseDto>>> getAllUsers(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all users request, page: {}, size: {}", page, size);
        GenericApiResponse<List<AdminUserResponseDto>> response = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> getUserById(@PathVariable Long userId) {
        log.debug("Admin: Get user by ID request for: {}", userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> suspendUser(@PathVariable Long userId) {
        log.debug("Admin: Suspend user request for: {}", userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.suspendUser(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> activateUser(@PathVariable Long userId) {
        log.debug("Admin: Activate user request for: {}", userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.activateUser(userId);
        return ResponseEntity.ok(response);
    }

    // ==================== EVENT MODERATION ====================

    @GetMapping("/events")
    public ResponseEntity<GenericApiResponse<List<EventResponseDto>>> getAllEvents(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all events request, page: {}, size: {}", page, size);
        GenericApiResponse<List<EventResponseDto>> response = adminService.getAllEvents(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/events/{eventId}/remove")
    public ResponseEntity<GenericApiResponse<Void>> removeEvent(@PathVariable Long eventId) {
        log.debug("Admin: Remove event request for: {}", eventId);
        GenericApiResponse<Void> response = adminService.removeEvent(eventId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<GenericApiResponse<Void>> deleteEvent(@PathVariable Long eventId) {
        log.debug("Admin: Delete event permanently request for: {}", eventId);
        GenericApiResponse<Void> response = adminService.deleteEvent(eventId);
        return ResponseEntity.ok(response);
    }

    // ==================== REPORT HANDLING ====================

    @GetMapping("/reports")
    public ResponseEntity<GenericApiResponse<List<ReportResponseDto>>> getAllReports() {
        log.debug("Admin: Get all reports request");
        GenericApiResponse<List<ReportResponseDto>> response = adminService.getAllReports();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/pending")
    public ResponseEntity<GenericApiResponse<List<ReportResponseDto>>> getPendingReports() {
        log.debug("Admin: Get pending reports request");
        GenericApiResponse<List<ReportResponseDto>> response = adminService.getPendingReports();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<GenericApiResponse<ReportResponseDto>> resolveReport(@PathVariable Long reportId) {
        log.debug("Admin: Resolve report request for: {}", reportId);
        GenericApiResponse<ReportResponseDto> response = adminService.resolveReport(reportId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reports/{reportId}/reject")
    public ResponseEntity<GenericApiResponse<ReportResponseDto>> rejectReport(@PathVariable Long reportId) {
        log.debug("Admin: Reject report request for: {}", reportId);
        GenericApiResponse<ReportResponseDto> response = adminService.rejectReport(reportId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/dashboard/stats")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> getDashboardStats() {
        log.debug("Admin: Get dashboard statistics request");
        GenericApiResponse<Map<String, Object>> response = adminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }

    // ==================== GROUP MANAGEMENT ====================

    @GetMapping("/groups")
    public ResponseEntity<GenericApiResponse<List<GroupResponseDto>>> getAllGroups(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all groups request, page: {}, size: {}", page, size);
        GenericApiResponse<List<GroupResponseDto>> response = adminService.getAllGroups(page, size);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<GenericApiResponse<Void>> deleteGroup(@PathVariable Long groupId) {
        log.debug("Admin: Delete group request for: {}", groupId);
        GenericApiResponse<Void> response = adminService.deleteGroup(groupId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}/memberships")
    public ResponseEntity<GenericApiResponse<List<GroupMembershipResponseDto>>> getGroupMemberships(
            @PathVariable Long groupId
    ) {
        log.debug("Admin: Get group memberships request for group: {}", groupId);
        GenericApiResponse<List<GroupMembershipResponseDto>> response = adminService.getGroupMemberships(groupId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/groups/memberships/{membershipId}/approve")
    public ResponseEntity<GenericApiResponse<GroupMembershipResponseDto>> approveMembership(
            @PathVariable Long membershipId
    ) {
        log.debug("Admin: Approve membership request for: {}", membershipId);
        GenericApiResponse<GroupMembershipResponseDto> response = adminService.approveMembership(membershipId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/groups/memberships/{membershipId}/reject")
    public ResponseEntity<GenericApiResponse<Void>> rejectMembership(@PathVariable Long membershipId) {
        log.debug("Admin: Reject membership request for: {}", membershipId);
        GenericApiResponse<Void> response = adminService.rejectMembership(membershipId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/groups/memberships/{membershipId}/ban")
    public ResponseEntity<GenericApiResponse<GroupMembershipResponseDto>> banMember(@PathVariable Long membershipId) {
        log.debug("Admin: Ban member request for: {}", membershipId);
        GenericApiResponse<GroupMembershipResponseDto> response = adminService.banMember(membershipId);
        return ResponseEntity.ok(response);
    }

    // ==================== PAYMENT MANAGEMENT ====================

    @GetMapping("/payments")
    public ResponseEntity<GenericApiResponse<List<PaymentResponseDto>>> getAllPayments(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all payments request, page: {}, size: {}", page, size);
        GenericApiResponse<List<PaymentResponseDto>> response = adminService.getAllPayments(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/stats")
    public ResponseEntity<GenericApiResponse<Map<String, Object>>> getPaymentStats() {
        log.debug("Admin: Get payment statistics request");
        GenericApiResponse<Map<String, Object>> response = adminService.getPaymentStats();
        return ResponseEntity.ok(response);
    }

    // ==================== ENROLLMENT MANAGEMENT ====================

    @GetMapping("/enrollments")
    public ResponseEntity<GenericApiResponse<List<EventEnrollmentResponseDto>>> getAllEnrollments(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Admin: Get all enrollments request, page: {}, size: {}", page, size);
        GenericApiResponse<List<EventEnrollmentResponseDto>> response = adminService.getAllEnrollments(page, size);
        return ResponseEntity.ok(response);
    }

    // ==================== ROLE MANAGEMENT ====================

    @PostMapping("/users/{userId}/assign-role")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> assignRole(
            @PathVariable Long userId,
            @RequestParam String roleName
    ) {
        log.debug("Admin: Assign role {} to user {}", roleName, userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.assignRole(userId, roleName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/remove-role")
    public ResponseEntity<GenericApiResponse<AdminUserResponseDto>> removeRole(
            @PathVariable Long userId,
            @RequestParam String roleName
    ) {
        log.debug("Admin: Remove role {} from user {}", roleName, userId);
        GenericApiResponse<AdminUserResponseDto> response = adminService.removeRole(userId, roleName);
        return ResponseEntity.ok(response);
    }
}
