package com.mahmoud.coursehub.controller;

import com.mahmoud.coursehub.dto.EnrollmentRequest;
import com.mahmoud.coursehub.dto.EnrollmentUpdateRequest;
import com.mahmoud.coursehub.model.Enrollment;
import com.mahmoud.coursehub.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @GetMapping
    public List<Enrollment> getAll() {
        return enrollmentService.getAll();
    }

    @GetMapping("/{id}")
    public Enrollment getById(@PathVariable Long id) {
        return enrollmentService.getById(id);
    }

    // GET /api/enrollments/student/4 -> everything a student follows
    @GetMapping("/student/{studentId}")
    public List<Enrollment> byStudent(@PathVariable Long studentId) {
        return enrollmentService.getByStudent(studentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Enrollment create(@Valid @RequestBody EnrollmentRequest request) {
        return enrollmentService.create(request);
    }

    @PutMapping("/{id}")
    public Enrollment update(@PathVariable Long id, @Valid @RequestBody EnrollmentUpdateRequest request) {
        return enrollmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        enrollmentService.delete(id);
    }

    public EnrollmentController(final EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }
}
