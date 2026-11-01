package com.mahmoud.coursehub.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull(message = "studentId is required") Long studentId,
        @NotNull(message = "courseId is required") Long courseId
) {
}
