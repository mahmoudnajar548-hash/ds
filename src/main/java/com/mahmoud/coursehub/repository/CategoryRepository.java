package com.mahmoud.coursehub.repository;

import com.mahmoud.coursehub.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Derived query: Spring builds the SQL from the method name
    Optional<Category> findByNameIgnoreCase(String name);
}
