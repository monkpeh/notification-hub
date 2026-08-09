package com.notifyhub.security;

import java.time.Instant;

public record RateLimitExceededResponse(String message, Instant resetAt) {}
