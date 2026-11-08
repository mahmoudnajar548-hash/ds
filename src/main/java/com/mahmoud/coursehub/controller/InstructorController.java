package com.mahmoud.coursehub.controller;

import com.mahmoud.coursehub.model.Instructor;
import com.mahmoud.coursehub.service.InstructorService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/instructors")
public class InstructorController {
    private final InstructorService instructorService;

    @GetMapping
    public List<Instructor> getAll() {
        return instructorService.getAll();
    }

    @GetMapping("/{id}")
    public Instructor getById(@PathVariable Long id) {
        return instructorService.getById(id);
    }

    @GetMapping("/{id}/revenue")
    public Map<String, Object> getRevenue(@PathVariable Long id) {
        return instructorService.getRevenue(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Instructor create(@Valid @RequestBody Instructor instructor) {
        return instructorService.create(instructor);
    }

    @PutMapping("/{id}")
    public Instructor update(@PathVariable Long id, @Valid @RequestBody Instructor instructor) {
        return instructorService.update(id, instructor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        instructorService.delete(id);
    }

    public InstructorController(final InstructorService instructorService) {
        this.instructorService = instructorService;
    }
}
