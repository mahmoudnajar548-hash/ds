package com.mahmoud.coursehub.dto;

import com.mahmoud.coursehub.model.EnrollmentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Both fields are optional: send only what you want to change. */
public record EnrollmentUpdateRequest(
        @Min(value = 0, message = "Progress cannot be below 0")
        @Max(value = 100, message = "Progress cannot exceed 100")
        Integer progress,

        EnrollmentStatus status
) {
}
