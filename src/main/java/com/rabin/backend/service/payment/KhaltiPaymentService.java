package com.rabin.backend.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabin.backend.dto.response.KhaltiInitiateResponseDto;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KhaltiPaymentService {

    @Value("${app.payment.khalti.secret-key}")
    private String secretKey;

    @Value("${app.payment.khalti.api-url}")
    private String apiUrl;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initiate Khalti payment
     */
    public KhaltiInitiateResponseDto initiatePayment(Payment payment, Event event, String returnUrl) {
        try {
            String purchaseOrderId = UUID.randomUUID().toString();
            payment.setTransactionId(purchaseOrderId);

            // Prepare request payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("return_url", baseUrl + "/api/payments/khalti/verify");
            payload.put("website_url", baseUrl);
            payload.put("amount", (int) (payment.getAmount() * 100)); // Convert to paisa (smallest unit)
            payload.put("purchase_order_id", purchaseOrderId);
            payload.put("purchase_order_name", event.getTitle());

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Key " + secretKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/initiate/",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());

                KhaltiInitiateResponseDto responseDto = new KhaltiInitiateResponseDto();
                responseDto.setPidx(responseJson.get("pidx").asText());
                responseDto.setPayment_url(responseJson.get("payment_url").asText());

                if (responseJson.has("expires_at")) {
                    responseDto.setExpires_at(responseJson.get("expires_at").asLong());
                }
                if (responseJson.has("expires_in")) {
                    responseDto.setExpires_in(responseJson.get("expires_in").asInt());
                }

                // Store pidx in payment data for verification
                payment.setPaymentData(responseDto.getPidx());

                return responseDto;
            } else {
                throw new RuntimeException("Failed to initiate Khalti payment: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initiating Khalti payment: " + e.getMessage(), e);
        }
    }

    /**
     * Verify Khalti payment
     */
    public boolean verifyPayment(String pidx) {
        try {
            // Prepare request payload
            Map<String, String> payload = new HashMap<>();
            payload.put("pidx", pidx);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Key " + secretKey);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + "/lookup/",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                String status = responseJson.get("status").asText();
                return "Completed".equalsIgnoreCase(status);
            }

            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error verifying Khalti payment: " + e.getMessage(), e);
        }
    }
}
