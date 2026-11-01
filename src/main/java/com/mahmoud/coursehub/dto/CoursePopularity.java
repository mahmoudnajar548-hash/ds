package com.mahmoud.coursehub.dto;

/** One row of the "most popular courses" ranking (built directly by a JPQL query). */
public record CoursePopularity(Long courseId, String title, Long enrollments) {
}
