package com.rabin.backend.controller;

import com.rabin.backend.dto.request.PaymentInitiateDto;
import com.rabin.backend.model.Payment;
import com.rabin.backend.service.payment.PaymentService;
import com.rabin.backend.util.RedirectUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @PostMapping("/initiate")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER')")
    public ResponseEntity<?> initiatePayment(@RequestBody PaymentInitiateDto dto) {
        try {
            Object response = paymentService.initiatePayment(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Verify Khalti payment
     * Log all parameters to debug CR/LF issue
     */
    @GetMapping("/khalti/verify")
    public void verifyKhaltiPayment(
            @RequestParam Map<String, String> allParams,
            HttpServletResponse response) throws IOException {

        log.info("=== KHALTI PAYMENT CALLBACK ===");
        log.info("Received parameters: {}", allParams);

        try {
            // Log each parameter for debugging
            allParams.forEach((key, value) -> {
                if (value != null) {
                    // Check for problematic characters
                    if (value.contains("\n") || value.contains("\r")) {
                        log.warn("Parameter '{}' contains CR/LF characters. Original: '{}'", key, value);
                        // Clean the value
                        String cleaned = value.replace("\n", "").replace("\r", "");
                        allParams.put(key, cleaned);
                    }
                }
            });

            String pidx = allParams.get("pidx");
            if (pidx == null || pidx.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing pidx parameter");
            }

            Payment payment = paymentService.verifyKhaltiPayment(pidx);

            // Use callback URL from payment or fallback to default
            String baseCallbackUrl = payment.getCallbackUrl() != null ?
                    payment.getCallbackUrl() : frontendUrl + "/payment/callback";

            // Build parameters map with SAFE values
            Map<String, String> redirectParams = new HashMap<>();
            redirectParams.put("status", safeValue(String.valueOf(payment.getPaymentStatus())));
            redirectParams.put("paymentId", safeValue(payment.getId().toString()));
            redirectParams.put("eventId", safeValue(payment.getEvent().getId().toString()));
            redirectParams.put("transactionId", safeValue(payment.getTransactionId()));

            // Include event info for ticket/QR construction
            redirectParams.put("eventTitle", safeValue(payment.getEvent().getTitle()));
            if (payment.getEvent().getVenue() != null) {
                redirectParams.put("venue", safeValue(payment.getEvent().getVenue()));
            }
            if (payment.getEvent().getStartDate() != null) {
                redirectParams.put("eventDate", safeValue(payment.getEvent().getStartDate().toString()));
            }

            if (payment.getEnrollment() != null) {
                redirectParams.put("enrolled", "true");
                redirectParams.put("ticketCode", safeValue(payment.getEnrollment().getTicketCode()));
                redirectParams.put("enrollmentId", safeValue(payment.getEnrollment().getId().toString()));
            }

            // Build safe redirect URL
            String redirectUrl = RedirectUtil.buildSafeRedirectUrl(baseCallbackUrl, redirectParams);

            log.info("Redirecting to: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Payment verification failed: {}", e.getMessage(), e);

            // Build error redirect URL with SAFE error message
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("status", "FAILED");
            errorParams.put("error", safeValue(e.getMessage()));

            String errorRedirectUrl = RedirectUtil.buildSafeRedirectUrl(
                    frontendUrl + "/payment/callback",
                    errorParams
            );

            log.info("Error redirect to: {}", errorRedirectUrl);
            response.sendRedirect(errorRedirectUrl);
        }
    }

    @GetMapping("/esewa/verify")
    public void verifyEsewaPayment(
            @RequestParam Map<String, String> allParams,
            HttpServletResponse response) throws IOException {

        log.info("=== ESEWA PAYMENT CALLBACK ===");
        log.info("Received parameters: {}", allParams);

        try {
            // Clean parameters
            allParams.forEach((key, value) -> {
                if (value != null && (value.contains("\n") || value.contains("\r"))) {
                    String cleaned = value.replace("\n", "").replace("\r", "");
                    allParams.put(key, cleaned);
                }
            });

            String transactionUuid = allParams.get("transaction_uuid");
            String totalAmount = allParams.get("total_amount");
            String productCode = allParams.get("product_code");
            String signature = allParams.get("signature");

            if (transactionUuid == null || totalAmount == null || productCode == null || signature == null) {
                throw new IllegalArgumentException("Missing required eSewa parameters");
            }

            Payment payment = paymentService.verifyEsewaPayment(
                    transactionUuid, totalAmount, productCode, signature);

            String baseCallbackUrl = payment.getCallbackUrl() != null ?
                    payment.getCallbackUrl() : frontendUrl + "/payment/callback";

            Map<String, String> redirectParams = new HashMap<>();
            redirectParams.put("status", safeValue(String.valueOf(payment.getPaymentStatus())));
            redirectParams.put("paymentId", safeValue(payment.getId().toString()));
            redirectParams.put("eventId", safeValue(payment.getEvent().getId().toString()));
            redirectParams.put("transactionId", safeValue(payment.getTransactionId()));

            // Include event info for ticket/QR construction
            redirectParams.put("eventTitle", safeValue(payment.getEvent().getTitle()));
            if (payment.getEvent().getVenue() != null) {
                redirectParams.put("venue", safeValue(payment.getEvent().getVenue()));
            }
            if (payment.getEvent().getStartDate() != null) {
                redirectParams.put("eventDate", safeValue(payment.getEvent().getStartDate().toString()));
            }

            if (payment.getEnrollment() != null) {
                redirectParams.put("enrolled", "true");
                redirectParams.put("ticketCode", safeValue(payment.getEnrollment().getTicketCode()));
                redirectParams.put("enrollmentId", safeValue(payment.getEnrollment().getId().toString()));
            }

            String redirectUrl = RedirectUtil.buildSafeRedirectUrl(baseCallbackUrl, redirectParams);
            log.info("Redirecting to: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("eSewa payment verification failed: {}", e.getMessage(), e);

            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("status", "FAILED");
            errorParams.put("error", safeValue(e.getMessage()));

            String errorRedirectUrl = RedirectUtil.buildSafeRedirectUrl(
                    frontendUrl + "/payment/callback",
                    errorParams
            );

            log.info("Error redirect to: {}", errorRedirectUrl);
            response.sendRedirect(errorRedirectUrl);
        }
    }

    @GetMapping("/status/{transactionId}")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER')")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String transactionId) {
        try {
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", payment.getPaymentStatus());
            response.put("amount", payment.getAmount());
            response.put("paymentMethod", payment.getPaymentMethod());
            response.put("createdAt", payment.getCreatedAt());
            response.put("completedAt", payment.getCompletedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Helper method to ensure string values don't contain CR/LF
     */
    private String safeValue(String value) {
        if (value == null) return "";
        // Remove any CR/LF characters
        return value.replace("\r", "").replace("\n", "");
    }
}