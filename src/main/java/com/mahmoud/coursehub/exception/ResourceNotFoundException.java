package com.mahmoud.coursehub.exception;

/** Thrown when an id does not exist. Becomes HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " with id " + id + " not found");
    }
}
