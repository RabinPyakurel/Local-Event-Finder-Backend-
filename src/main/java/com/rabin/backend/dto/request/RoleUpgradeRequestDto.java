package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Role upgrade request payload for users wanting to become organizers")
public class RoleUpgradeRequestDto {
    @Schema(description = "Reason why user wants to become an organizer", example = "I want to organize tech meetups and community events in my city", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
