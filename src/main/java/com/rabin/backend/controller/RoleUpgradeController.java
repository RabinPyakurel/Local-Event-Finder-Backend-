package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.RoleUpgradeRequestDto;
import com.rabin.backend.dto.response.RoleUpgradeResponseDto;
import com.rabin.backend.service.RoleUpgradeService;
import com.rabin.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/role-upgrade")
@Slf4j
@Tag(name = "Role Upgrade", description = "APIs for users to request organizer role upgrade")
@SecurityRequirement(name = "bearerAuth")
public class RoleUpgradeController {

    private final RoleUpgradeService roleUpgradeService;

    public RoleUpgradeController(RoleUpgradeService roleUpgradeService) {
        this.roleUpgradeService = roleUpgradeService;
    }

    @Operation(summary = "Submit role upgrade request", description = "Submit a request to become an organizer. Admin will review and approve/reject.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Pending request already exists or already an organizer"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GenericApiResponse<RoleUpgradeResponseDto>> submitRequest(
            @RequestBody RoleUpgradeRequestDto dto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Role upgrade request from userId: {}", userId);

        RoleUpgradeResponseDto response = roleUpgradeService.submitRequest(userId, dto);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Request submitted successfully. You will be notified once reviewed.", response)
        );
    }

    @Operation(summary = "Get my requests", description = "Get the current user's role upgrade request history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requests retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER')")
    public ResponseEntity<GenericApiResponse<List<RoleUpgradeResponseDto>>> getMyRequests() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Getting role upgrade requests for userId: {}", userId);

        List<RoleUpgradeResponseDto> requests = roleUpgradeService.getMyRequests(userId);

        return ResponseEntity.ok(
                GenericApiResponse.ok(200, "Requests retrieved successfully", requests)
        );
    }
}
