package com.mahmoud.coursehub.service;

import com.mahmoud.coursehub.exception.ConflictException;
import com.mahmoud.coursehub.exception.ResourceNotFoundException;
import com.mahmoud.coursehub.model.Category;
import com.mahmoud.coursehub.repository.CategoryRepository;
import com.mahmoud.coursehub.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Transactional
    public Category create(Category category) {
        checkNameIsUnique(category.getName(), null);
        category.setId(null); // never trust an id sent by the client on creation
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, Category data) {
        Category existing = getById(id);
        checkNameIsUnique(data.getName(), id);
        existing.setName(data.getName());
        existing.setDescription(data.getDescription());
        return categoryRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        if (courseRepository.existsByCategoryId(id)) {
            throw new ConflictException("Cannot delete category \'" + category.getName() + "\': it still contains courses");
        }
        categoryRepository.delete(category);
    }

    // currentId = the category being updated (null on creation), so it can keep its own name
    private void checkNameIsUnique(String name, Long currentId) {
        categoryRepository.findByNameIgnoreCase(name).ifPresent(other -> {
            if (!other.getId().equals(currentId)) {
                throw new ConflictException("A category named \'" + name + "\' already exists");
            }
        });
    }

    public CategoryService(final CategoryRepository categoryRepository, final CourseRepository courseRepository) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
    }
}
