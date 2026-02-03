package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ticket verification request payload")
public class TicketVerifyRequestDto {
    @Schema(description = "Ticket code to verify (from QR code)", example = "TKT-ABC123XYZ", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ticketCode;
}
