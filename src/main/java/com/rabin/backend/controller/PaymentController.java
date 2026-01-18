package com.rabin.backend.controller;

import com.rabin.backend.dto.request.PaymentInitiateDto;
import com.rabin.backend.model.Payment;
import com.rabin.backend.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Initiate payment for an event
     * Returns KhaltiInitiateResponseDto for Khalti or EsewaPaymentFormDto for eSewa
     */
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
     * Called by Khalti after user completes payment
     */
    @GetMapping("/khalti/verify")
    public ResponseEntity<?> verifyKhaltiPayment(@RequestParam("pidx") String pidx) {
        try {
            Payment payment = paymentService.verifyKhaltiPayment(pidx);

            Map<String, Object> response = new HashMap<>();
            response.put("status", payment.getPaymentStatus());
            response.put("paymentId", payment.getId());
            response.put("transactionId", payment.getTransactionId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Verify eSewa payment
     * Called by eSewa after user completes payment
     */
    @GetMapping("/esewa/verify")
    public ResponseEntity<?> verifyEsewaPayment(
            @RequestParam("transaction_uuid") String transactionUuid,
            @RequestParam("total_amount") String totalAmount,
            @RequestParam("product_code") String productCode,
            @RequestParam("signature") String signature) {
        try {
            Payment payment = paymentService.verifyEsewaPayment(
                    transactionUuid, totalAmount, productCode, signature);

            Map<String, Object> response = new HashMap<>();
            response.put("status", payment.getPaymentStatus());
            response.put("paymentId", payment.getId());
            response.put("transactionId", payment.getTransactionId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get payment status by transaction ID
     */
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
}
