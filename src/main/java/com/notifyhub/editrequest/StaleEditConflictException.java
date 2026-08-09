package com.notifyhub.editrequest;

public class StaleEditConflictException extends RuntimeException {

    private final String currentValue;
    private final String requestedOldValue;
    private final String requestedNewValue;

    public StaleEditConflictException(String currentValue, String requestedOldValue, String requestedNewValue) {
        super("Edit request is stale — the underlying value has changed since submission");
        this.currentValue = currentValue;
        this.requestedOldValue = requestedOldValue;
        this.requestedNewValue = requestedNewValue;
    }

    public String getCurrentValue() { return currentValue; }
    public String getRequestedOldValue() { return requestedOldValue; }
    public String getRequestedNewValue() { return requestedNewValue; }
}
