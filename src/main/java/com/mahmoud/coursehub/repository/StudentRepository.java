package com.mahmoud.coursehub.repository;

import com.mahmoud.coursehub.model.EnrollmentStatus;
import com.mahmoud.coursehub.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmailIgnoreCase(String email);

    // CUSTOM QUERY 2 - class roster: students of a course (cancelled enrollments excluded),
    // joining through the Student.enrollments relationship
    @Query("""
            SELECT s FROM Student s JOIN s.enrollments e
            WHERE e.course.id = :courseId AND e.status <> :excluded
            ORDER BY s.fullName ASC
            """)
    List<Student> findStudentsOfCourse(@Param("courseId") Long courseId,
                                       @Param("excluded") EnrollmentStatus excluded);
}
