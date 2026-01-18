package com.rabin.backend.repository;

import com.rabin.backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user.email = ?1 AND t.used = false")
    void invalidateExistingTokens(String email);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryTime < ?1")
    void deleteExpiredTokens(Instant now);
}
