package com.mahmoud.coursehub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * A rating (1-5) and optional comment written by a student about a course.
 * One review per student per course.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(name = "uk_review_student_course", columnNames = {"student_id", "course_id"}))
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    @NotNull(message = "Course is required")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating;
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Column(length = 1000)
    private String comment;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return this.id;
    }

    public Student getStudent() {
        return this.student;
    }

    public Course getCourse() {
        return this.course;
    }

    public Integer getRating() {
        return this.rating;
    }

    public String getComment() {
        return this.comment;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setStudent(final Student student) {
        this.student = student;
    }

    public void setCourse(final Course course) {
        this.course = course;
    }

    public void setRating(final Integer rating) {
        this.rating = rating;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Review() {
    }
}
