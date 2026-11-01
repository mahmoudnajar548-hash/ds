package com.mahmoud.coursehub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * A subject area (Programming, Design, Data...). One category contains many courses.
 */
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 80, message = "Category name must be between 2 and 80 characters")
    @Column(nullable = false, unique = true, length = 80)
    private String name;
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    // Inverse side of Course.category. @JsonIgnore avoids infinite JSON recursion.
    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<Course> courses = new ArrayList<>();

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<Course> getCourses() {
        return this.courses;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setCourses(final List<Course> courses) {
        this.courses = courses;
    }

    public Category() {
    }
}
