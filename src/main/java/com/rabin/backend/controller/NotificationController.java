package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.NotificationResponseDto;
import com.rabin.backend.service.NotificationService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "APIs for managing in-app notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notifications", description = "Get paginated notifications for the current user, ordered by newest first")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Page<NotificationResponseDto>>> getNotifications(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<NotificationResponseDto> notifications = notificationService.getNotifications(userId, page, size);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Notifications retrieved successfully", notifications)
        );
    }

    @Operation(summary = "Get unread notification count", description = "Get the count of unread notifications for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Map<String, Long>>> getUnreadCount() {
        Long userId = SecurityUtil.getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Unread count retrieved", Map.of("unreadCount", count))
        );
    }

    @Operation(summary = "Mark notification as read", description = "Mark a single notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<NotificationResponseDto>> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable Long id
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        NotificationResponseDto notification = notificationService.markAsRead(id, userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Notification marked as read", notification)
        );
    }

    @Operation(summary = "Mark all notifications as read", description = "Mark all unread notifications as read for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Map<String, Integer>>> markAllAsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        int count = notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "All notifications marked as read", Map.of("markedCount", count))
        );
    }

    @Operation(summary = "Delete notification", description = "Delete a notification. Only the recipient can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericApiResponse<Void>> deleteNotification(
            @Parameter(description = "Notification ID") @PathVariable Long id
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.deleteNotification(id, userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Notification deleted", null)
        );
    }
}
