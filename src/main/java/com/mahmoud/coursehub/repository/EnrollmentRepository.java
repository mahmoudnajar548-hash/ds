package com.mahmoud.coursehub.repository;

import com.mahmoud.coursehub.dto.CoursePopularity;
import com.mahmoud.coursehub.model.Enrollment;
import com.mahmoud.coursehub.model.EnrollmentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);

    boolean existsByCourseId(Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // "Not" keyword: status <> given status
    boolean existsByStudentIdAndCourseIdAndStatusNot(Long studentId, Long courseId, EnrollmentStatus status);

    // CUSTOM QUERY 3 - popularity ranking: number of enrollments per course, biggest first.
    // "SELECT new ..." builds a DTO directly; Pageable limits the number of rows.
    @Query("""
            SELECT new com.mahmoud.coursehub.dto.CoursePopularity(c.id, c.title, COUNT(e))
            FROM Enrollment e JOIN e.course c
            WHERE e.status <> :excluded
            GROUP BY c.id, c.title
            ORDER BY COUNT(e) DESC, c.title ASC
            """)
    List<CoursePopularity> findMostPopularCourses(@Param("excluded") EnrollmentStatus excluded, Pageable pageable);

    // CUSTOM QUERY 4 - instructor revenue: sum of prices of the enrollments of his courses
    @Query("""
            SELECT SUM(c.price)
            FROM Enrollment e JOIN e.course c
            WHERE c.instructor.id = :instructorId AND e.status <> :excluded
            """)
    BigDecimal sumRevenueByInstructor(@Param("instructorId") Long instructorId,
                                      @Param("excluded") EnrollmentStatus excluded);
}
