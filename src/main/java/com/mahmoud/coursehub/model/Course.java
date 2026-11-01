package com.mahmoud.coursehub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The central entity: a course taught by one instructor, in one category,
 * with many tags, followed by many students (through Enrollment).
 */
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    @Column(nullable = false, length = 150)
    private String title;
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(length = 2000)
    private String description;
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    @Max(value = 1000, message = "Duration must not exceed 1000 hours")
    @Column(nullable = false)
    private Integer durationHours;
    @NotNull(message = "Level is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Level level;
    // MANY courses -> ONE category (owning side: holds the category_id foreign key)
    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    // MANY courses -> ONE instructor
    @NotNull(message = "Instructor is required")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;
    // MANY courses <-> MANY tags (owning side: defines the course_tags join table)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "course_tags", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();
    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();
    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public Integer getDurationHours() {
        return this.durationHours;
    }

    public Level getLevel() {
        return this.level;
    }

    public Category getCategory() {
        return this.category;
    }

    public Instructor getInstructor() {
        return this.instructor;
    }

    public Set<Tag> getTags() {
        return this.tags;
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

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public void setDurationHours(final Integer durationHours) {
        this.durationHours = durationHours;
    }

    public void setLevel(final Level level) {
        this.level = level;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    public void setInstructor(final Instructor instructor) {
        this.instructor = instructor;
    }

    public void setTags(final Set<Tag> tags) {
        this.tags = tags;
    }

    public void setEnrollments(final List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public void setReviews(final List<Review> reviews) {
        this.reviews = reviews;
    }

    public Course() {
    }
}
