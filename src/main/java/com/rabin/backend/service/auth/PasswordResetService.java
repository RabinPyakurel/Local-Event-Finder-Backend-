package com.rabin.backend.service.auth;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.ForgotPasswordRequestDto;
import com.rabin.backend.dto.request.ResetPasswordRequestDto;
import com.rabin.backend.dto.response.PasswordResetResponseDto;
import com.rabin.backend.exception.InvalidTokenException;
import com.rabin.backend.exception.UserNotFoundException;
import com.rabin.backend.model.PasswordResetToken;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.PasswordResetTokenRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.util.EmailUtil;
import com.rabin.backend.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailUtil emailService;
    private final PasswordEncoder passwordEncoder;
    private final com.rabin.backend.service.RateLimitService rateLimitService;

    private static final int OTP_VALIDITY_MINUTES = 10;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                EmailUtil emailService,
                                PasswordEncoder passwordEncoder,
                                com.rabin.backend.service.RateLimitService rateLimitService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.rateLimitService = rateLimitService;
    }

    @Transactional
    public GenericApiResponse<PasswordResetResponseDto> forgotPassword(ForgotPasswordRequestDto dto) {
        log.debug("Forgot password request for email: {}", dto.getEmail());

        // Check rate limit before processing
        try {
            rateLimitService.checkRateLimit("password_reset_" + dto.getEmail());
        } catch (IllegalStateException e) {
            log.warn("Rate limit exceeded for password reset: {}", dto.getEmail());
            return GenericApiResponse.error(429, e.getMessage());
        }

        if (!ValidationUtil.isValidEmail(dto.getEmail())) {
            log.warn("Forgot password failed - invalid email format: {}", dto.getEmail());
            return GenericApiResponse.error(400, "Invalid email format");
        }

        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());

        PasswordResetResponseDto responseData = new PasswordResetResponseDto(
                "If the email exists, a reset OTP has been sent"
        );

        if (userOpt.isEmpty()) {
            log.debug("Forgot password request for non-existent email: {}", dto.getEmail());
            return GenericApiResponse.ok(200, "Request processed", responseData);
        }

        User user = userOpt.get();

        // Invalidate existing tokens safely
        try {
            tokenRepository.invalidateExistingTokens(dto.getEmail());
            log.debug("Invalidated existing tokens for email: {}", dto.getEmail());
        } catch (Exception e) {
            log.warn("Could not invalidate existing tokens for email {}: {}", dto.getEmail(), e.getMessage());
        }

        // Generate OTP
        String otp = generateOtp();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(otp);
        token.setUser(user);
        token.setExpiryTime(LocalDateTime.now().plusSeconds(OTP_VALIDITY_MINUTES * 60));
        tokenRepository.save(token);

        // Send email safely
        try {
            emailService.sendPasswordResetEmail(dto.getEmail(), otp, user.getFullName());
            log.info("Password reset OTP sent to email: {}", dto.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", dto.getEmail(), e.getMessage());
        }

        return GenericApiResponse.ok(200, "Request processed", responseData);
    }

    @Transactional
    public GenericApiResponse<PasswordResetResponseDto> resetPassword(ResetPasswordRequestDto dto) {
        log.debug("Reset password attempt with token: {}", dto.getToken());

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            log.warn("Reset password failed - passwords don't match for token: {}", dto.getToken());
            return GenericApiResponse.error(400, "Passwords do not match");
        }

        String pwdError = ValidationUtil.validatePassword(dto.getNewPassword());
        if (pwdError != null) {
            throw new IllegalArgumentException(pwdError);
        }

        PasswordResetToken token = tokenRepository.findByTokenAndUsedFalse(dto.getToken())
                .orElseThrow(() -> {
                    log.warn("Reset password failed - invalid or used OTP: {}", dto.getToken());
                    return new InvalidTokenException("Invalid or expired OTP");
                });

        User user = token.getUser();
        if (user == null) {
            log.error("Reset password failed - user not found for token: {}", dto.getToken());
            throw new UserNotFoundException("User not found");
        }

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("Reset password failed - OTP expired for user: {}", user.getId());
            throw new InvalidTokenException("OTP has expired");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        // Clear rate limit after successful password reset
        rateLimitService.clearRateLimit("password_reset_" + user.getEmail());

        log.info("Password reset successfully for user: {}", user.getId());
        PasswordResetResponseDto responseData = new PasswordResetResponseDto("Password reset successfully");
        return GenericApiResponse.ok(200, "Password reset completed", responseData);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1000000));
    }
}

