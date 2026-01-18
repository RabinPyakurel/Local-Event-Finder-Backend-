package com.rabin.backend.dto.response;

import lombok.Data;

@Data
public class EsewaPaymentFormDto {
    private String amount;
    private String taxAmount;
    private String totalAmount;
    private String transactionUuid;
    private String productCode;
    private String productServiceCharge;
    private String productDeliveryCharge;
    private String successUrl;
    private String failureUrl;
    private String signedFieldNames;
    private String signature;
    private String formUrl;  // eSewa form submission URL
}
