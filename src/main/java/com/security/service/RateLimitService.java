package com.security.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Instant;

@Service
public class RateLimitService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60000;

    private final ConcurrentHashMap<String, AtomicInteger> attemptCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStart = new ConcurrentHashMap<>();

    public boolean isRateLimited(String ip) {
        long now = Instant.now().toEpochMilli();
        windowStart.putIfAbsent(ip, now);

        if (now - windowStart.get(ip) > WINDOW_MS) {
            windowStart.put(ip, now);
            attemptCounts.put(ip, new AtomicInteger(0));
        }

        attemptCounts.putIfAbsent(ip, new AtomicInteger(0));
        int attempts = attemptCounts.get(ip).incrementAndGet();
        return attempts > MAX_ATTEMPTS;
    }

    public void resetAttempts(String ip) {
        attemptCounts.remove(ip);
        windowStart.remove(ip);
    }
}