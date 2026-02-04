package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.GroupRequestDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.dto.response.GroupMembershipResponseDto;
import com.rabin.backend.dto.response.GroupResponseDto;
import com.rabin.backend.service.GroupService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "APIs for managing groups and group memberships")
@SecurityRequirement(name = "bearerAuth")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "Create a group", description = "Create a new group. Only ORGANIZER or ADMIN can create groups.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only organizers or admins can create groups")
    })
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

    @Operation(summary = "Update a group", description = "Update an existing group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this group"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PutMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> updateGroup(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
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

    @Operation(summary = "Get group details", description = "Get details of a specific group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group details retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getGroup(
            @Parameter(description = "Group ID") @PathVariable Long groupId
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

    @Operation(summary = "Get all groups", description = "Get all active groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getAllGroups() {
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

    @Operation(summary = "Get my groups", description = "Get groups the current user is a member of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User groups retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-groups")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getUserGroups() {
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

    @Operation(summary = "Join a group", description = "Join a group as a member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined group or request pending approval"),
            @ApiResponse(responseCode = "400", description = "Already a member or banned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/join")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> joinGroup(
            @Parameter(description = "Group ID") @PathVariable Long groupId
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

    @Operation(summary = "Leave a group", description = "Leave a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully left group"),
            @ApiResponse(responseCode = "400", description = "Not a member of this group"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/{groupId}/leave")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> leaveGroup(
            @Parameter(description = "Group ID") @PathVariable Long groupId
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

    @Operation(summary = "Get group members", description = "Get all members of a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group members retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/members")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getGroupMembers(
            @Parameter(description = "Group ID") @PathVariable Long groupId) {
        try {
            List<GroupMembershipResponseDto> members = groupService.getGroupMembers(groupId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "Add event to group", description = "Add an event to a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event added to group successfully"),
            @ApiResponse(responseCode = "400", description = "Event already in group or invalid"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not authorized to add events to this group"),
            @ApiResponse(responseCode = "404", description = "Group or event not found")
    })
    @PostMapping("/{groupId}/events/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> addEventToGroup(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @Parameter(description = "Event ID") @PathVariable Long eventId,
            @Parameter(description = "Whether the event is private to group members") @RequestParam(required = false, defaultValue = "false") Boolean isPrivate
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

    @Operation(summary = "Invite all followers to group", description = "Send group invitation to all your followers. They will receive a notification and can accept or decline. Already active, banned, or pending members are skipped.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitations sent successfully"),
            @ApiResponse(responseCode = "400", description = "No followers to invite or not a group member"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/invite-followers")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Map<String, Integer>>> inviteAllFollowersToGroup(
            @Parameter(description = "Group ID") @PathVariable Long groupId
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        int invitedCount = groupService.inviteAllFollowersToGroup(groupId, currentUserId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, invitedCount + " invitation(s) sent", Map.of("invitedCount", invitedCount))
        );
    }

    @Operation(summary = "Accept group invitation", description = "Accept a pending group invitation. Changes membership status from PENDING to ACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation accepted successfully"),
            @ApiResponse(responseCode = "400", description = "No pending invitation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/accept-invite")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<GroupMembershipResponseDto>> acceptGroupInvite(
            @Parameter(description = "Group ID") @PathVariable Long groupId
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        GroupMembershipResponseDto membership = groupService.acceptGroupInvite(groupId, currentUserId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Invitation accepted successfully", membership)
        );
    }

    @Operation(summary = "Decline group invitation", description = "Decline a pending group invitation. Removes the pending membership.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation declined"),
            @ApiResponse(responseCode = "400", description = "No pending invitation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/{groupId}/decline-invite")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<GenericApiResponse<Void>> declineGroupInvite(
            @Parameter(description = "Group ID") @PathVariable Long groupId
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        groupService.declineGroupInvite(groupId, currentUserId);
        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Invitation declined", null)
        );
    }

    @Operation(summary = "Get group events", description = "Get all events in a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group events retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/events")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<?> getGroupEvents(
            @Parameter(description = "Group ID") @PathVariable Long groupId
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
