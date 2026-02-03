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

    /**
     * Generic method to send email with custom subject and body
     */
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.debug("Sent email to {} with subject: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
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

    /**
     * Notify admins about new role upgrade request
     */
    public void sendRoleUpgradeRequestNotification(String adminEmail, String adminName,
                                                    String requesterName, String requesterEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmail);
            message.setSubject("New Organizer Role Request");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                    "A new organizer role upgrade request has been submitted.\n\n" +
                    "Requester: %s\n" +
                    "Email: %s\n\n" +
                    "Please review the request in the admin dashboard.\n\n" +
                    "Best regards,\nEvent Finder System",
                    adminName, requesterName, requesterEmail
            ));
            mailSender.send(message);
            log.debug("Sent role upgrade notification to admin {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to send role upgrade notification to {}: {}", adminEmail, e.getMessage());
        }
    }

    /**
     * Notify user about role upgrade approval
     */
    public void sendRoleUpgradeApprovalEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Congratulations! You are now an Organizer");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                    "Great news! Your request to become an organizer has been approved.\n\n" +
                    "You can now:\n" +
                    "- Create and manage events\n" +
                    "- View event enrollments and analytics\n" +
                    "- Verify tickets at your events\n\n" +
                    "Start creating amazing events today!\n\n" +
                    "Best regards,\nEvent Finder Team",
                    fullName
            ));
            mailSender.send(message);
            log.debug("Sent role upgrade approval email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send approval email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Notify user about role upgrade rejection
     */
    public void sendRoleUpgradeRejectionEmail(String toEmail, String fullName, String adminNote) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Organizer Request Update");

            String noteSection = (adminNote != null && !adminNote.trim().isEmpty())
                    ? String.format("\n\nAdmin's note: %s", adminNote)
                    : "";

            message.setText(String.format(
                    "Hello %s,\n\n" +
                    "Thank you for your interest in becoming an organizer.\n\n" +
                    "After careful review, we are unable to approve your request at this time.%s\n\n" +
                    "You may submit a new request after 30 days. In the meantime, continue " +
                    "engaging with events to build your activity profile.\n\n" +
                    "Best regards,\nEvent Finder Team",
                    fullName, noteSection
            ));
            mailSender.send(message);
            log.debug("Sent role upgrade rejection email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send rejection email to {}: {}", toEmail, e.getMessage());
        }
    }
}
