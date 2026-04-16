package com.rabin.backend.service.payment;

import com.rabin.backend.dto.request.PaymentInitiateDto;
import com.rabin.backend.dto.response.EsewaPaymentFormDto;
import com.rabin.backend.dto.response.KhaltiInitiateResponseDto;
import com.rabin.backend.enums.NotificationType;
import com.rabin.backend.enums.PaymentMethod;
import com.rabin.backend.enums.PaymentStatus;
import com.rabin.backend.enums.TicketStatus;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventEnrollment;
import com.rabin.backend.model.Payment;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventEnrollmentRepository;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.PaymentRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.service.NotificationService;
import com.rabin.backend.util.SecurityUtil;
import com.rabin.backend.util.TicketCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final EventEnrollmentRepository enrollmentRepository;
    private final KhaltiPaymentService khaltiPaymentService;
    private final EsewaPaymentService esewaPaymentService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

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

        // Reuse existing PENDING payment for this user+event, or create a new one
        Payment payment = paymentRepository.findFirstByUser_IdAndEvent_IdAndPaymentStatus(
                userId, dto.getEventId(), PaymentStatus.PENDING
        ).orElse(null);

        if (payment != null) {
            log.info("Reusing existing PENDING payment {} for user {} event {}",
                    payment.getId(), userId, dto.getEventId());
            // Update payment method and callback URL in case they changed
            payment.setPaymentMethod(dto.getPaymentMethod());
            payment.setCallbackUrl(dto.getReturnUrl());
        } else {
            payment = new Payment();
            payment.setUser(user);
            payment.setEvent(event);
            payment.setAmount(event.getPrice());
            payment.setPaymentMethod(dto.getPaymentMethod());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setCallbackUrl(dto.getReturnUrl());
        }

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
     * Verify Khalti payment and auto-enroll user
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

            // Auto-enroll user after successful payment
            EventEnrollment enrollment = autoEnrollUser(payment);
            payment.setEnrollment(enrollment);

            log.info("Payment verified and user auto-enrolled. PaymentId: {}, EnrollmentId: {}",
                    payment.getId(), enrollment.getId());

            // Notify user of successful payment
            notificationService.sendNotification(
                    payment.getUser().getId(),
                    NotificationType.PAYMENT_COMPLETED,
                    "Payment Successful",
                    "Your payment for event '" + payment.getEvent().getTitle() + "' has been completed successfully",
                    payment.getEvent().getId(),
                    "EVENT"
            );
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }

        return paymentRepository.save(payment);
    }

    /**
     * Verify eSewa payment and auto-enroll user
     */
    @Transactional
    public Payment verifyEsewaPayment(Map<String, String> esewaParams) {
        String transactionUuid = esewaParams.get("transaction_uuid");
        String esewaStatus = esewaParams.get("status");

        log.info("Verifying eSewa payment: transactionUuid={}, status={}, allParams={}",
                transactionUuid, esewaStatus, esewaParams.keySet());

        // Find payment by transaction ID
        Payment payment = paymentRepository.findByTransactionId(transactionUuid)
                .orElseThrow(() -> {
                    log.error("Payment not found for transactionId: {}", transactionUuid);
                    return new RuntimeException("Payment not found for transaction: " + transactionUuid);
                });

        log.info("Found payment: id={}, currentStatus={}, eventId={}",
                payment.getId(), payment.getPaymentStatus(), payment.getEvent().getId());

        // Check eSewa status first
        if (!"COMPLETE".equals(esewaStatus)) {
            log.warn("eSewa payment not complete. Status: '{}'", esewaStatus);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            return paymentRepository.save(payment);
        }

        // Verify signature using the full eSewa response
        boolean verified = esewaPaymentService.verifyPayment(esewaParams);
        log.info("eSewa signature verification result: {}", verified);

        if (verified) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());

            // Auto-enroll user after successful payment
            EventEnrollment enrollment = autoEnrollUser(payment);
            payment.setEnrollment(enrollment);

            log.info("Payment verified and user auto-enrolled. PaymentId: {}, EnrollmentId: {}",
                    payment.getId(), enrollment.getId());

            // Notify user of successful payment
            notificationService.sendNotification(
                    payment.getUser().getId(),
                    NotificationType.PAYMENT_COMPLETED,
                    "Payment Successful",
                    "Your payment for event '" + payment.getEvent().getTitle() + "' has been completed successfully",
                    payment.getEvent().getId(),
                    "EVENT"
            );
        } else {
            log.error("eSewa signature verification FAILED for payment: {}", payment.getId());
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }

        return paymentRepository.save(payment);
    }

    /**
     * Auto-enroll user after successful payment
     */
    private EventEnrollment autoEnrollUser(Payment payment) {
        User user = payment.getUser();
        Event event = payment.getEvent();

        // Check if already enrolled (return first existing ticket)
        if (enrollmentRepository.existsByUser_IdAndEvent_Id(user.getId(), event.getId())) {
            log.warn("User {} already enrolled in event {}", user.getId(), event.getId());
            EventEnrollment existingEnrollment = enrollmentRepository.findFirstByUser_IdAndEvent_Id(user.getId(), event.getId())
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));

            // Clear enrollment reference from any old payment to avoid unique constraint violation
            paymentRepository.findByEnrollment(existingEnrollment).ifPresent(oldPayment -> {
                if (!oldPayment.getId().equals(payment.getId())) {
                    log.info("Clearing enrollment reference from old payment {} to reassign to payment {}",
                            oldPayment.getId(), payment.getId());
                    oldPayment.setEnrollment(null);
                    paymentRepository.save(oldPayment);
                }
            });

            return existingEnrollment;
        }

        // Create enrollment
        EventEnrollment enrollment = new EventEnrollment();
        enrollment.setUser(user);
        enrollment.setEvent(event);
        enrollment.setTicketCode(TicketCodeGenerator.generate());
        enrollment.setTicketStatus(TicketStatus.ACTIVE);

        enrollment = enrollmentRepository.save(enrollment);

        // Update booked seats (null-safe)
        int currentBooked = event.getBookedSeats() != null ? event.getBookedSeats() : 0;
        event.setBookedSeats(currentBooked + 1);
        eventRepository.save(event);

        log.info("Auto-enrolled user {} in event {}. BookedSeats: {} -> {}",
                user.getId(), event.getId(), currentBooked, currentBooked + 1);

        return enrollment;
    }

    /**
     * Get payment by transaction ID
     */
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}
