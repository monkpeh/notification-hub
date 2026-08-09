package com.notifyhub.editrequest;

public record StaleEditConflictResponse(String message, String currentValue, String requestedOldValue, String requestedNewValue) {}
