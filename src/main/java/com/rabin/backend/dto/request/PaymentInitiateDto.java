package com.rabin.backend.dto.request;

import com.rabin.backend.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Payment initiation request payload")
public class PaymentInitiateDto {
    @Schema(description = "ID of the event to pay for", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long eventId;

    @Schema(description = "Payment method (KHALTI or ESEWA)", example = "KHALTI", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentMethod paymentMethod;

    @Schema(description = "Frontend URL to return after payment completion", example = "https://myapp.com/payment/callback", requiredMode = Schema.RequiredMode.REQUIRED)
    private String returnUrl;
}
