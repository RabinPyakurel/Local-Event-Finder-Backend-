package com.rabin.backend.dto.response;

import lombok.Data;

@Data
public class KhaltiInitiateResponseDto {
    private String pidx;  // Payment identifier from Khalti
    private String payment_url;  // URL to redirect user for payment
    private Long expires_at;
    private Integer expires_in;
}
