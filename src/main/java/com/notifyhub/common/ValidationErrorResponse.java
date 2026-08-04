package com.notifyhub.common;

import java.util.List;

public record ValidationErrorResponse(List<ValidationError> errors) {}
