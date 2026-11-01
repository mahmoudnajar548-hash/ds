package com.mahmoud.coursehub.repository;

import com.mahmoud.coursehub.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Derived queries (property path "category.id" is read from "CategoryId")
    List<Course> findByCategoryId(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByInstructorId(Long instructorId);

    // CUSTOM QUERY 1 - catalogue search: title contains a keyword AND price is under a budget
    @Query("""
            SELECT c FROM Course c
            WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              AND c.price <= :maxPrice
            ORDER BY c.price ASC
            """)
    List<Course> search(@Param("keyword") String keyword, @Param("maxPrice") BigDecimal maxPrice);
}
