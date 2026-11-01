package com.mahmoud.coursehub.repository;

import com.mahmoud.coursehub.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    Optional<Instructor> findByEmailIgnoreCase(String email);
}
