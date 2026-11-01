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
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * A teacher who creates courses. One instructor teaches many courses.
 */
@Entity
@Table(name = "instructors")
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String firstName;
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    @Column(length = 1000)
    private String bio;
    @OneToMany(mappedBy = "instructor")
    @JsonIgnore
    private List<Course> courses = new ArrayList<>();

    public Long getId() {
        return this.id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getBio() {
        return this.bio;
    }

    public List<Course> getCourses() {
        return this.courses;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setBio(final String bio) {
        this.bio = bio;
    }

    public void setCourses(final List<Course> courses) {
        this.courses = courses;
    }

    public Instructor() {
    }
}
