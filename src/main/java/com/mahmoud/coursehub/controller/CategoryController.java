package com.mahmoud.coursehub.controller;

import com.mahmoud.coursehub.model.Category;
import com.mahmoud.coursehub.service.CategoryService;
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
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAll();
    }

    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    // 201
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody Category category) {
        return categoryService.create(category);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @Valid @RequestBody Category category) {
        return categoryService.update(id, category);
    }

    // 204
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    public CategoryController(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }
}
