package com.notifyhub.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final Map<String, Integer> LIMITS_PER_HOUR = Map.of(
        "TEMPLATE_BUILDER", 20,
        "AI_AGENT", 20,
        "ADMIN", 50,
        "SUPER_ADMIN", 100
    );

    private final Map<Long, Deque<Instant>> submissionsByUser = new ConcurrentHashMap<>();

    public void checkAndRecord(CurrentUser user) {
        Integer limit = LIMITS_PER_HOUR.get(user.roleName());
        if (limit == null) {
            return;
        }

        Deque<Instant> timestamps = submissionsByUser.computeIfAbsent(user.userId(), id -> new ArrayDeque<>());

        synchronized (timestamps) {
            Instant now = Instant.now();
            Instant windowStart = now.minus(1, ChronoUnit.HOURS);

            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= limit) {
                Instant resetAt = timestamps.peekFirst().plus(1, ChronoUnit.HOURS);
                throw new RateLimitExceededException(resetAt);
            }

            timestamps.addLast(now);
        }
    }
}
