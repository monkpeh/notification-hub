package com.notifyhub.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(FieldValidationException ex) {
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(ex.getErrors()));
    }
}
