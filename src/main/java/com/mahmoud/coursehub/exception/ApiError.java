package com.mahmoud.coursehub.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** The JSON body returned for every error, so clients always see the same shape. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
