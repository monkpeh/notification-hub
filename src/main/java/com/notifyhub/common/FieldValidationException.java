package com.notifyhub.common;

import java.util.List;

public class FieldValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public FieldValidationException(List<ValidationError> errors) {
        super("Validation failed: " + errors.size() + " error(s)");
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
