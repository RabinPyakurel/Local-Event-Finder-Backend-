package com.rabin.backend.repository;

import com.rabin.backend.enums.PaymentStatus;
import com.rabin.backend.model.Payment;
import com.rabin.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser(User user);
    List<Payment> findByUserAndPaymentStatus(User user, PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
}
