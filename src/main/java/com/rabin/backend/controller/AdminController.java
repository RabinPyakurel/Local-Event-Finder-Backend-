package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.AdminUserResponseDto;
import com.rabin.backend.dto.response.EventResponseDto;
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
}
