package com.mahmoud.coursehub.exception;

/** Thrown when the request is well-formed but a business rule forbids it. Becomes HTTP 422. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
