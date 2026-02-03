package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Schema(description = "Create or update group request payload")
public class GroupRequestDto {
    @Schema(description = "Group name", example = "Tech Enthusiasts Nepal", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Group description", example = "A community for technology enthusiasts in Nepal")
    private String description;

    @Schema(description = "Group image file")
    private MultipartFile groupImage;

    @Schema(description = "Whether new members require approval to join", example = "false", defaultValue = "false")
    private Boolean requiresApproval;

    @Schema(description = "List of tag names or interest categories", example = "[\"TECHNOLOGY\", \"NETWORKING\"]")
    private List<String> tags;
}
