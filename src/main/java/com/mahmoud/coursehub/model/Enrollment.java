package com.mahmoud.coursehub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.LocalDateTime;

/**
 * Association entity between Student and Course.
 * It exists as its own entity because the link carries data (progress, status, date).
 * A student can be enrolled only once in a given course.
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(name = "uk_enrollment_student_course", columnNames = {"student_id", "course_id"}))
public class Enrollment {
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
    @Column(nullable = false)
    private LocalDateTime enrolledAt;
    @Min(value = 0, message = "Progress cannot be below 0")
    @Max(value = 100, message = "Progress cannot exceed 100")
    @Column(nullable = false)
    private int progress;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    public Long getId() {
        return this.id;
    }

    public Student getStudent() {
        return this.student;
    }

    public Course getCourse() {
        return this.course;
    }

    public LocalDateTime getEnrolledAt() {
        return this.enrolledAt;
    }

    public int getProgress() {
        return this.progress;
    }

    public EnrollmentStatus getStatus() {
        return this.status;
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

    public void setEnrolledAt(final LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public void setProgress(final int progress) {
        this.progress = progress;
    }

    public void setStatus(final EnrollmentStatus status) {
        this.status = status;
    }

    public Enrollment() {
    }
}
