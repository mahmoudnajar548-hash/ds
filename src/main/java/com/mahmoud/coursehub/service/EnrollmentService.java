package com.mahmoud.coursehub.service;

import com.mahmoud.coursehub.dto.EnrollmentRequest;
import com.mahmoud.coursehub.dto.EnrollmentUpdateRequest;
import com.mahmoud.coursehub.exception.BusinessRuleException;
import com.mahmoud.coursehub.exception.ConflictException;
import com.mahmoud.coursehub.exception.ResourceNotFoundException;
import com.mahmoud.coursehub.model.Course;
import com.mahmoud.coursehub.model.Enrollment;
import com.mahmoud.coursehub.model.EnrollmentStatus;
import com.mahmoud.coursehub.model.Student;
import com.mahmoud.coursehub.repository.CourseRepository;
import com.mahmoud.coursehub.repository.EnrollmentRepository;
import com.mahmoud.coursehub.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public List<Enrollment> getAll() {
        return enrollmentRepository.findAll();
    }

    public Enrollment getById(Long id) {
        return enrollmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
    }

    public List<Enrollment> getByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", studentId);
        }
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Transactional
    public Enrollment create(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.studentId()).orElseThrow(() -> new ResourceNotFoundException("Student", request.studentId()));
        Course course = courseRepository.findById(request.courseId()).orElseThrow(() -> new ResourceNotFoundException("Course", request.courseId()));
        // Business rule: one enrollment per student per course
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new ConflictException(student.getFullName() + " is already enrolled in \'" + course.getTitle() + "\'");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgress(0);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment update(Long id, EnrollmentUpdateRequest request) {
        Enrollment enrollment = getById(id);
        // Business rule: a cancelled enrollment is frozen
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new BusinessRuleException("A cancelled enrollment cannot be modified");
        }
        if (request.progress() != null) {
            enrollment.setProgress(request.progress());
        }
        if (request.status() != null) {
            enrollment.setStatus(request.status());
        }
        // Business rule: 100% progress means COMPLETED, and COMPLETED means 100% progress
        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE && enrollment.getProgress() == 100) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
        }
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            enrollment.setProgress(100);
        }
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void delete(Long id) {
        enrollmentRepository.delete(getById(id));
    }

    public EnrollmentService(final EnrollmentRepository enrollmentRepository, final StudentRepository studentRepository, final CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }
}
