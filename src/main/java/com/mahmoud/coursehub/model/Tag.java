package com.mahmoud.coursehub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * A keyword attached to courses (java, spring, beginner-friendly...).
 * Many-to-many with Course: a course has many tags, a tag labels many courses.
 */
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = 40, message = "Tag name must be between 2 and 40 characters")
    @Column(nullable = false, unique = true, length = 40)
    private String name;
    // Inverse side: the join table is owned by Course.tags
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Course> courses = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Set<Course> getCourses() {
        return this.courses;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setCourses(final Set<Course> courses) {
        this.courses = courses;
    }

    public Tag() {
    }
}
