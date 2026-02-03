package com.rabin.backend.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage blacklisted JWT tokens (for logout functionality)
 * Uses in-memory storage with automatic cleanup of expired entries
 * Note: For production with multiple instances, use Redis instead
 */
@Service
@Slf4j
public class TokenBlacklistService {

    // Map of token -> expiration timestamp
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist
     * @param token The JWT token to blacklist
     * @param expirationTimeMillis When the token would naturally expire
     */
    public void blacklistToken(String token, long expirationTimeMillis) {
        blacklist.put(token, expirationTimeMillis);
        log.debug("Token blacklisted, will be removed at {}", Instant.ofEpochMilli(expirationTimeMillis));
    }

    /**
     * Check if a token is blacklisted
     * @param token The JWT token to check
     * @return true if the token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * Clean up expired tokens from the blacklist every 10 minutes
     * Tokens are kept until their natural expiration time
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        int beforeSize = blacklist.size();

        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);

        int removed = beforeSize - blacklist.size();
        if (removed > 0) {
            log.info("Cleaned up {} expired tokens from blacklist", removed);
        }
    }

    /**
     * Get the current size of the blacklist (for monitoring)
     */
    public int getBlacklistSize() {
        return blacklist.size();
    }
}
