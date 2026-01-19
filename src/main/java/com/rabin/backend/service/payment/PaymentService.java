package com.rabin.backend.service.payment;

import com.rabin.backend.dto.request.PaymentInitiateDto;
import com.rabin.backend.dto.response.EsewaPaymentFormDto;
import com.rabin.backend.dto.response.KhaltiInitiateResponseDto;
import com.rabin.backend.enums.PaymentMethod;
import com.rabin.backend.enums.PaymentStatus;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.Payment;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.PaymentRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final KhaltiPaymentService khaltiPaymentService;
    private final EsewaPaymentService esewaPaymentService;
    private final UserRepository userRepository;

    /**
     * Initiate payment for an event
     */
    @Transactional
    public Object initiatePayment(PaymentInitiateDto dto) {
        // Get current user
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get event
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Validate event is paid
        if (!event.getIsPaid()) {
            throw new RuntimeException("This event is free, payment not required");
        }

        // Check available seats
        if (event.getAvailableSeats() != null &&
            event.getBookedSeats() >= event.getAvailableSeats()) {
            throw new RuntimeException("No seats available for this event");
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setEvent(event);
        payment.setAmount(event.getPrice());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);

        // Save payment
        payment = paymentRepository.save(payment);

        // Initiate payment based on method
        if (dto.getPaymentMethod() == PaymentMethod.KHALTI) {
            KhaltiInitiateResponseDto response = khaltiPaymentService.initiatePayment(
                    payment, event, dto.getReturnUrl());
            paymentRepository.save(payment);
            return response;
        } else if (dto.getPaymentMethod() == PaymentMethod.ESEWA) {
            EsewaPaymentFormDto response = esewaPaymentService.generatePaymentForm(
                    payment, event, dto.getReturnUrl());
            paymentRepository.save(payment);
            return response;
        } else {
            throw new RuntimeException("Invalid payment method");
        }
    }

    /**
     * Verify Khalti payment
     */
    @Transactional
    public Payment verifyKhaltiPayment(String pidx) {
        // Find payment by pidx
        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> pidx.equals(p.getPaymentData()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Verify with Khalti
        boolean verified = khaltiPaymentService.verifyPayment(pidx);

        if (verified) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            // Update booked seats
            Event event = payment.getEvent();
            event.setBookedSeats(event.getBookedSeats() + 1);
            eventRepository.save(event);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }

        return paymentRepository.save(payment);
    }

    /**
     * Verify eSewa payment
     */
    @Transactional
    public Payment verifyEsewaPayment(String transactionUuid, String totalAmount,
                                       String productCode, String signature) {
        // Find payment by transaction ID
        Payment payment = paymentRepository.findByTransactionId(transactionUuid)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Verify signature
        boolean verified = esewaPaymentService.verifyPayment(
                transactionUuid, totalAmount, productCode, signature);

        if (verified) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            // Update booked seats
            Event event = payment.getEvent();
            event.setBookedSeats(event.getBookedSeats() + 1);
            eventRepository.save(event);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }

        return paymentRepository.save(payment);
    }

    /**
     * Get payment by transaction ID
     */
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}
