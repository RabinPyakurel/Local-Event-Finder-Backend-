package com.rabin.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailUtil {

    private final JavaMailSender mailSender;

    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String otp, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP");
            message.setText(String.format(
                    "Hello %s,\n\nYour OTP for password reset is: %s\nIt will expire in 10 minutes.\n\nBest,\nEvent Finder Team",
                    fullName, otp
            ));
            mailSender.send(message);
            log.debug("Sent password reset email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send event cancellation notification to enrolled user
     */
    public void sendEventCancellationEmail(String toEmail, String fullName, String eventTitle,
                                            boolean isPaidEvent, Double refundAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Event Cancelled: " + eventTitle);

            String refundInfo = "";
            if (isPaidEvent && refundAmount != null && refundAmount > 0) {
                refundInfo = String.format(
                        "\n\nSince this was a paid event, your payment of Rs. %.2f will be refunded within 5-7 business days.",
                        refundAmount
                );
            }

            message.setText(String.format(
                    "Hello %s,\n\n" +
                    "We regret to inform you that the event \"%s\" has been cancelled by the organizer.\n" +
                    "Your ticket has been automatically cancelled.%s\n\n" +
                    "We apologize for any inconvenience caused.\n\n" +
                    "Best regards,\nEvent Finder Team",
                    fullName, eventTitle, refundInfo
            ));
            mailSender.send(message);
            log.debug("Sent event cancellation email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send cancellation email to {}: {}", toEmail, e.getMessage());
        }
    }
}
