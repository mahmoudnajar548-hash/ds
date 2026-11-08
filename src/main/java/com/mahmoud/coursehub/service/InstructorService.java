package com.mahmoud.coursehub.service;

import com.mahmoud.coursehub.exception.ConflictException;
import com.mahmoud.coursehub.exception.ResourceNotFoundException;
import com.mahmoud.coursehub.model.EnrollmentStatus;
import com.mahmoud.coursehub.model.Instructor;
import com.mahmoud.coursehub.repository.CourseRepository;
import com.mahmoud.coursehub.repository.EnrollmentRepository;
import com.mahmoud.coursehub.repository.InstructorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class InstructorService {
    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<Instructor> getAll() {
        return instructorRepository.findAll();
    }

    public Instructor getById(Long id) {
        return instructorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Instructor", id));
    }

    @Transactional
    public Instructor create(Instructor instructor) {
        checkEmailIsUnique(instructor.getEmail(), null);
        instructor.setId(null);
        return instructorRepository.save(instructor);
    }

    @Transactional
    public Instructor update(Long id, Instructor data) {
        Instructor existing = getById(id);
        checkEmailIsUnique(data.getEmail(), id);
        existing.setFirstName(data.getFirstName());
        existing.setLastName(data.getLastName());
        existing.setEmail(data.getEmail());
        existing.setBio(data.getBio());
        return instructorRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Instructor instructor = getById(id);
        if (courseRepository.existsByInstructorId(id)) {
            throw new ConflictException("Cannot delete instructor " + instructor.getFirstName() + " " + instructor.getLastName() + ": he/she still teaches courses");
        }
        instructorRepository.delete(instructor);
    }

    // Business query: how much did this instructor earn? (cancelled enrollments do not count)
    public Map<String, Object> getRevenue(Long id) {
        Instructor instructor = getById(id);
        BigDecimal total = enrollmentRepository.sumRevenueByInstructor(id, EnrollmentStatus.CANCELLED);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instructorId", instructor.getId());
        result.put("instructorName", instructor.getFirstName() + " " + instructor.getLastName());
        result.put("totalRevenue", total == null ? BigDecimal.ZERO : total); // SUM returns null when there is no row
        return result;
    }

    private void checkEmailIsUnique(String email, Long currentId) {
        instructorRepository.findByEmailIgnoreCase(email).ifPresent(other -> {
            if (!other.getId().equals(currentId)) {
                throw new ConflictException("An instructor with email \'" + email + "\' already exists");
            }
        });
    }

    public InstructorService(final InstructorRepository instructorRepository, final CourseRepository courseRepository, final EnrollmentRepository enrollmentRepository) {
        this.instructorRepository = instructorRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }
}
