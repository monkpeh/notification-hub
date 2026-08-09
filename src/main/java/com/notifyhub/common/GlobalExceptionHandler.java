package com.notifyhub.common;

import com.notifyhub.editrequest.StaleEditConflictException;
import com.notifyhub.editrequest.StaleEditConflictResponse;
import com.notifyhub.security.RateLimitExceededException;
import com.notifyhub.security.RateLimitExceededResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(FieldValidationException ex) {
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(ex.getErrors()));
    }

    @ExceptionHandler(StaleEditConflictException.class)
    public ResponseEntity<StaleEditConflictResponse> handleStaleConflict(StaleEditConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new StaleEditConflictResponse(
            ex.getMessage(), ex.getCurrentValue(), ex.getRequestedOldValue(), ex.getRequestedNewValue()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<RateLimitExceededResponse> handleRateLimit(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new RateLimitExceededResponse(
            ex.getMessage(), ex.getResetAt()));
    }
}
