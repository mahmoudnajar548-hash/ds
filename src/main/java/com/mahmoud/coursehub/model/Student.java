package com.mahmoud.coursehub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A learner. Students follow courses through Enrollment and write Reviews.
 */
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String fullName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    @OneToMany(mappedBy = "student")
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();
    @OneToMany(mappedBy = "student")
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    public Long getId() {
        return this.id;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getEmail() {
        return this.email;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public List<Enrollment> getEnrollments() {
        return this.enrollments;
    }

    public List<Review> getReviews() {
        return this.reviews;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setBirthDate(final LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setEnrollments(final List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public void setReviews(final List<Review> reviews) {
        this.reviews = reviews;
    }

    public Student() {
    }
}
