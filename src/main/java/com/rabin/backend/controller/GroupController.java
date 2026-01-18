package com.rabin.backend.controller;

import com.rabin.backend.dto.request.GroupRequestDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.dto.response.GroupMembershipResponseDto;
import com.rabin.backend.dto.response.GroupResponseDto;
import com.rabin.backend.service.GroupService;
import com.rabin.backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing groups and group memberships
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * Create a new group
     * Only ORGANIZER or ADMIN can create groups
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> createGroup(@ModelAttribute GroupRequestDto dto) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            GroupResponseDto group = groupService.createGroup(currentUserId, dto);
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update a group
     */
    @PutMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> updateGroup(
            @PathVariable Long groupId,
            @ModelAttribute GroupRequestDto dto
) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            GroupResponseDto group = groupService.updateGroup(groupId, currentUserId, dto);
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get group details
     */
    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getGroup(
            @PathVariable Long groupId
) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            GroupResponseDto group = groupService.getGroup(groupId, currentUserId);
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all active groups
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getAllGroups(
) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            List<GroupResponseDto> groups = groupService.getAllGroups(currentUserId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get groups user is member of
     */
    @GetMapping("/my-groups")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getUserGroups(
) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            List<GroupResponseDto> groups = groupService.getUserGroups(currentUserId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Join a group
     */
    @PostMapping("/{groupId}/join")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> joinGroup(
            @PathVariable Long groupId
) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            GroupMembershipResponseDto membership = groupService.joinGroup(groupId, currentUserId);
            return ResponseEntity.ok(membership);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Leave a group
     */
    @DeleteMapping("/{groupId}/leave")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> leaveGroup(
            @PathVariable Long groupId
) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            groupService.leaveGroup(groupId, currentUserId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully left group");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get group members
     */
    @GetMapping("/{groupId}/members")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getGroupMembers(@PathVariable Long groupId) {

        try {
            List<GroupMembershipResponseDto> members = groupService.getGroupMembers(groupId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Add event to group
     */
    @PostMapping("/{groupId}/events/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> addEventToGroup(
            @PathVariable Long groupId,
            @PathVariable Long eventId,
            @RequestParam(required = false, defaultValue = "false") Boolean isPrivate
) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            groupService.addEventToGroup(groupId, eventId, currentUserId, isPrivate);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Event added to group successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get events for a group
     */
    @GetMapping("/{groupId}/events")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getGroupEvents(
            @PathVariable Long groupId
) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            List<EventResponseDto> events = groupService.getGroupEvents(groupId, currentUserId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

}
