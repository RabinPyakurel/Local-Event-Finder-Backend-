package com.rabin.backend.service.payment;

import com.rabin.backend.dto.response.EsewaPaymentFormDto;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EsewaPaymentService {

    @Value("${app.payment.esewa.secret-key}")
    private String secretKey;

    @Value("${app.payment.esewa.product-code}")
    private String productCode;

    @Value("${app.payment.esewa.form-url}")
    private String formUrl;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Generate eSewa payment form data with signature
     */
    public EsewaPaymentFormDto generatePaymentForm(Payment payment, Event event, String returnUrl) {
        String transactionUuid = UUID.randomUUID().toString();
        payment.setTransactionId(transactionUuid);

        EsewaPaymentFormDto formDto = new EsewaPaymentFormDto();

        // Set amounts
        String amount = String.format("%.2f", payment.getAmount());
        String taxAmount = "0.00";
        String totalAmount = amount;

        formDto.setAmount(amount);
        formDto.setTaxAmount(taxAmount);
        formDto.setTotalAmount(totalAmount);
        formDto.setTransactionUuid(transactionUuid);
        formDto.setProductCode(productCode);
        formDto.setProductServiceCharge("0.00");
        formDto.setProductDeliveryCharge("0.00");

        // Set callback URLs - no query params on success URL since eSewa appends ?data=base64...
        String successUrl = baseUrl + "/api/payments/esewa/verify";
        String failureUrl = returnUrl + "?status=failed&transaction_uuid=" + transactionUuid;

        formDto.setSuccessUrl(successUrl);
        formDto.setFailureUrl(failureUrl);

        // Set signed field names
        String signedFieldNames = "total_amount,transaction_uuid,product_code";
        formDto.setSignedFieldNames(signedFieldNames);

        // Generate signature
        String message = String.format("total_amount=%s,transaction_uuid=%s,product_code=%s",
                totalAmount, transactionUuid, productCode);
        String signature = generateHmacSHA256Signature(message);
        formDto.setSignature(signature);

        // Set form URL
        formDto.setFormUrl(formUrl);

        return formDto;
    }

    /**
     * Generate HMAC-SHA256 signature for eSewa
     */
    private String generateHmacSHA256Signature(String message) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac.init(secretKeySpec);

            byte[] hashBytes = hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Verify eSewa payment signature using signed_field_names from the response
     */
    public boolean verifyPayment(Map<String, String> esewaResponse) {
        String signedFieldNames = esewaResponse.get("signed_field_names");
        String signature = esewaResponse.get("signature");

        if (signedFieldNames == null || signature == null) {
            return false;
        }

        // Build message from signed_field_names in the exact order specified
        StringBuilder message = new StringBuilder();
        String[] fields = signedFieldNames.split(",");
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) message.append(",");
            message.append(fields[i]).append("=").append(esewaResponse.getOrDefault(fields[i], ""));
        }

        String expectedSignature = generateHmacSHA256Signature(message.toString());
        return expectedSignature.equals(signature);
    }
}
