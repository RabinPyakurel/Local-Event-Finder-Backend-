package com.rabin.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Payment details response")
public class PaymentResponseDto {
    @Schema(description = "Payment ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "User's name", example = "John Doe")
    private String userName;

    @Schema(description = "User's email", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Event ID", example = "1")
    private Long eventId;

    @Schema(description = "Event title", example = "Tech Conference 2024")
    private String eventTitle;

    @Schema(description = "Payment amount in NPR", example = "500.00")
    private Double amount;

    @Schema(description = "Payment method used", example = "KHALTI")
    private String paymentMethod;

    @Schema(description = "Payment status", example = "COMPLETED")
    private String paymentStatus;

    @Schema(description = "Transaction ID from payment gateway", example = "TXN-ABC123XYZ")
    private String transactionId;

    @Schema(description = "Payment initiation timestamp", example = "2024-05-20T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Payment completion timestamp", example = "2024-05-20T14:35:00")
    private LocalDateTime completedAt;

    @Schema(description = "Whether refund has been processed", example = "false")
    private Boolean refundProcessed;

    @Schema(description = "Refund timestamp", example = "2024-05-25T10:00:00")
    private LocalDateTime refundedAt;

    @Schema(description = "Admin note for refund", example = "Event cancelled by organizer")
    private String refundNote;
}
