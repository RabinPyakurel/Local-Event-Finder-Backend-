package com.rabin.backend.repository;

import com.rabin.backend.enums.PaymentStatus;
import com.rabin.backend.model.EventEnrollment;
import com.rabin.backend.model.Payment;
import com.rabin.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser(User user);
    List<Payment> findByUserAndPaymentStatus(User user, PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByEnrollment(EventEnrollment enrollment);

    // Count methods for admin dashboard
    long countByPaymentStatus(PaymentStatus status);

    // Find payments pending refund (status = REFUNDED but not yet processed)
    List<Payment> findByPaymentStatusAndRefundProcessed(PaymentStatus status, Boolean refundProcessed);

    // Count pending refunds for dashboard
    long countByPaymentStatusAndRefundProcessed(PaymentStatus status, Boolean refundProcessed);

    // Delete all payments for an event
    void deleteByEvent_Id(Long eventId);
}
