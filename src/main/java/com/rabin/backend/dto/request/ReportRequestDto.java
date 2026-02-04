package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Report an event request payload")
public class ReportRequestDto {
    @Schema(description = "Reason for reporting the event", example = "This event contains misleading information", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
