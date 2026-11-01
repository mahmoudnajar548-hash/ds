package com.mahmoud.coursehub.dto;

import com.mahmoud.coursehub.model.Level;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

/**
 * What the client sends to create/update a course.
 * Relations are given as ids (categoryId, instructorId, tagIds), not as nested objects.
 */
public record CourseRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
        String title,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price cannot be negative")
        BigDecimal price,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 hour")
        @Max(value = 1000, message = "Duration must not exceed 1000 hours")
        Integer durationHours,

        @NotNull(message = "Level is required")
        Level level,

        @NotNull(message = "categoryId is required")
        Long categoryId,

        @NotNull(message = "instructorId is required")
        Long instructorId,

        Set<Long> tagIds
) {
}
