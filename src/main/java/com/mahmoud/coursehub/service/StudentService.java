package com.mahmoud.coursehub.service;

import com.mahmoud.coursehub.exception.ConflictException;
import com.mahmoud.coursehub.exception.ResourceNotFoundException;
import com.mahmoud.coursehub.model.Student;
import com.mahmoud.coursehub.repository.EnrollmentRepository;
import com.mahmoud.coursehub.repository.ReviewRepository;
import com.mahmoud.coursehub.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;

    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public Student getById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    @Transactional
    public Student create(Student student) {
        checkEmailIsUnique(student.getEmail(), null);
        student.setId(null);
        return studentRepository.save(student);
    }

    @Transactional
    public Student update(Long id, Student data) {
        Student existing = getById(id);
        checkEmailIsUnique(data.getEmail(), id);
        existing.setFullName(data.getFullName());
        existing.setEmail(data.getEmail());
        existing.setBirthDate(data.getBirthDate());
        return studentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Student student = getById(id);
        if (enrollmentRepository.existsByStudentId(id) || reviewRepository.existsByStudentId(id)) {
            throw new ConflictException("Cannot delete student \'" + student.getFullName() + "\': he/she still has enrollments or reviews");
        }
        studentRepository.delete(student);
    }

    private void checkEmailIsUnique(String email, Long currentId) {
        studentRepository.findByEmailIgnoreCase(email).ifPresent(other -> {
            if (!other.getId().equals(currentId)) {
                throw new ConflictException("A student with email \'" + email + "\' already exists");
            }
        });
    }

    public StudentService(final StudentRepository studentRepository, final EnrollmentRepository enrollmentRepository, final ReviewRepository reviewRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.reviewRepository = reviewRepository;
    }
}
