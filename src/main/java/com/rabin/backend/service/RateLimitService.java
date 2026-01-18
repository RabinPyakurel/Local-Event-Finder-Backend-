package com.rabin.backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, RequestTracker> requestTracker = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 3;
    private static final int TIME_WINDOW_MINUTES = 15;

    public void checkRateLimit(String identifier) {
        RequestTracker tracker = requestTracker.computeIfAbsent(identifier, k -> new RequestTracker());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(TIME_WINDOW_MINUTES);

        // Reset if outside time window
        if (tracker.firstRequestTime.isBefore(windowStart)) {
            tracker.reset(now);
        }

        // Check if limit exceeded
        if (tracker.attemptCount >= MAX_ATTEMPTS) {
            LocalDateTime resetTime = tracker.firstRequestTime.plusMinutes(TIME_WINDOW_MINUTES);
            long minutesUntilReset = java.time.Duration.between(now, resetTime).toMinutes();
            throw new IllegalStateException(
                String.format("Too many requests. Please try again in %d minutes.", minutesUntilReset + 1)
            );
        }

        tracker.attemptCount++;
    }

    public void clearRateLimit(String identifier) {
        requestTracker.remove(identifier);
    }

    private static class RequestTracker {
        LocalDateTime firstRequestTime;
        int attemptCount;

        RequestTracker() {
            reset(LocalDateTime.now());
        }

        void reset(LocalDateTime time) {
            this.firstRequestTime = time;
            this.attemptCount = 0;
        }
    }
}
