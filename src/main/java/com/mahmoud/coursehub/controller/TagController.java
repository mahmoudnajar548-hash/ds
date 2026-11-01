package com.mahmoud.coursehub.controller;

import com.mahmoud.coursehub.model.Tag;
import com.mahmoud.coursehub.service.TagService;
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
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;

    @GetMapping
    public List<Tag> getAll() {
        return tagService.getAll();
    }

    @GetMapping("/{id}")
    public Tag getById(@PathVariable Long id) {
        return tagService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tag create(@Valid @RequestBody Tag tag) {
        return tagService.create(tag);
    }

    @PutMapping("/{id}")
    public Tag update(@PathVariable Long id, @Valid @RequestBody Tag tag) {
        return tagService.update(id, tag);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tagService.delete(id);
    }

    public TagController(final TagService tagService) {
        this.tagService = tagService;
    }
}
