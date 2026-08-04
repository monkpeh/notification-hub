package com.notifyhub.common;

public record ValidationError(String fieldName, Object rejectedValue, String reason) {}
