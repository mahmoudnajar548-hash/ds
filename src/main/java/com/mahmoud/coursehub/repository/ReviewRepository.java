package com.mahmoud.coursehub.repository;

import com.mahmoud.coursehub.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCourseId(Long courseId);

    boolean existsByStudentId(Long studentId);

    boolean existsByCourseId(Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByCourseId(Long courseId);

    // CUSTOM QUERY 5 - quality indicator: average rating of a course (null if no review)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);
}
