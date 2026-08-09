package com.notifyhub.security;

import java.time.Instant;

public class RateLimitExceededException extends RuntimeException {

    private final Instant resetAt;

    public RateLimitExceededException(Instant resetAt) {
        super("Rate limit exceeded — try again after " + resetAt);
        this.resetAt = resetAt;
    }

    public Instant getResetAt() {
        return resetAt;
    }
}
