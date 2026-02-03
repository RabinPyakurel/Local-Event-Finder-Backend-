package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "eSewa payment form data for frontend submission")
public class EsewaPaymentFormDto {
    @Schema(description = "Payment amount", example = "500.00")
    private String amount;

    @Schema(description = "Tax amount", example = "0")
    private String taxAmount;

    @Schema(description = "Total amount including tax", example = "500.00")
    private String totalAmount;

    @Schema(description = "Unique transaction identifier", example = "TXN-ABC123XYZ")
    private String transactionUuid;

    @Schema(description = "eSewa merchant product code", example = "EPAYTEST")
    private String productCode;

    @Schema(description = "Service charge", example = "0")
    private String productServiceCharge;

    @Schema(description = "Delivery charge", example = "0")
    private String productDeliveryCharge;

    @Schema(description = "Success callback URL", example = "https://api.example.com/api/payments/esewa/verify")
    private String successUrl;

    @Schema(description = "Failure callback URL", example = "https://example.com/payment/failed")
    private String failureUrl;

    @Schema(description = "Comma-separated list of signed field names", example = "total_amount,transaction_uuid,product_code")
    private String signedFieldNames;

    @Schema(description = "HMAC signature for verification", example = "abc123signature...")
    private String signature;

    @Schema(description = "eSewa payment form submission URL", example = "https://rc-epay.esewa.com.np/api/epay/main/v2/form")
    private String formUrl;
}
