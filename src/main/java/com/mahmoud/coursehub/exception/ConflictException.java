package com.mahmoud.coursehub.exception;

/** Thrown on duplicates or when a delete is blocked by existing data. Becomes HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
