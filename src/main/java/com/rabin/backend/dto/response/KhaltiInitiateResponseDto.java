package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Khalti payment initiation response")
public class KhaltiInitiateResponseDto {
    @Schema(description = "Payment identifier from Khalti", example = "pidx_abc123xyz")
    private String pidx;

    @Schema(description = "URL to redirect user for payment", example = "https://khalti.com/payment/...")
    private String payment_url;

    @Schema(description = "Payment link expiration timestamp", example = "2024-05-20T15:00:00")
    private String expires_at;

    @Schema(description = "Expiration time in seconds", example = "1800")
    private Integer expires_in;
}
