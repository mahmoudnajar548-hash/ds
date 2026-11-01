package com.mahmoud.coursehub.dto;

/** Average rating of a course. averageRating is null when nobody reviewed it yet. */
public record CourseRating(Long courseId, String title, Double averageRating, long reviewCount) {
}
