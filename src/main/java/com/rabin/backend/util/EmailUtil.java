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
}
