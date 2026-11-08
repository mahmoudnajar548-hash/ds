package com.mahmoud.coursehub.controller;

import com.mahmoud.coursehub.model.Course;
import com.mahmoud.coursehub.model.Student;
import com.mahmoud.coursehub.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;

    // ----- CRUD -----
    @GetMapping
    public List<Course> getAll() {
        return courseService.getAll();
    }

    @GetMapping("/{id}")
    public Course getById(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Course create(@Valid @RequestBody Course course) {
        return courseService.create(course);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @Valid @RequestBody Course course) {
        return courseService.update(id, course);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }

    // ----- business endpoints (backed by the custom queries) -----
    // GET /api/courses/search?keyword=spring&maxPrice=100
    @GetMapping("/search")
    public List<Course> search(@RequestParam(defaultValue = "") String keyword, @RequestParam(required = false) BigDecimal maxPrice) {
        return courseService.search(keyword, maxPrice);
    }

    // GET /api/courses/popular?limit=5
    @GetMapping("/popular")
    public List<Map<String, Object>> popular(@RequestParam(defaultValue = "5") int limit) {
        return courseService.getMostPopular(limit);
    }

    // GET /api/courses/category/3
    @GetMapping("/category/{categoryId}")
    public List<Course> byCategory(@PathVariable Long categoryId) {
        return courseService.getByCategory(categoryId);
    }

    // GET /api/courses/7/students
    @GetMapping("/{id}/students")
    public List<Student> students(@PathVariable Long id) {
        return courseService.getStudents(id);
    }

    // GET /api/courses/7/rating
    @GetMapping("/{id}/rating")
    public Map<String, Object> rating(@PathVariable Long id) {
        return courseService.getRating(id);
    }

    public CourseController(final CourseService courseService) {
        this.courseService = courseService;
    }
}
