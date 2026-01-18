package com.rabin.backend.dto.request;

import com.rabin.backend.enums.PaymentMethod;
import lombok.Data;

@Data
public class PaymentInitiateDto {
    private Long eventId;
    private PaymentMethod paymentMethod;  // KHALTI or ESEWA
    private String returnUrl;  // Frontend URL to return after payment
}
