package com.mahmoud.coursehub.service;

import com.mahmoud.coursehub.dto.ReviewRequest;
import com.mahmoud.coursehub.dto.ReviewUpdateRequest;
import com.mahmoud.coursehub.exception.BusinessRuleException;
import com.mahmoud.coursehub.exception.ConflictException;
import com.mahmoud.coursehub.exception.ResourceNotFoundException;
import com.mahmoud.coursehub.model.Course;
import com.mahmoud.coursehub.model.EnrollmentStatus;
import com.mahmoud.coursehub.model.Review;
import com.mahmoud.coursehub.model.Student;
import com.mahmoud.coursehub.repository.CourseRepository;
import com.mahmoud.coursehub.repository.EnrollmentRepository;
import com.mahmoud.coursehub.repository.ReviewRepository;
import com.mahmoud.coursehub.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<Review> getAll() {
        return reviewRepository.findAll();
    }

    public Review getById(Long id) {
        return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review", id));
    }

    public List<Review> getByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }
        return reviewRepository.findByCourseId(courseId);
    }

    @Transactional
    public Review create(ReviewRequest request) {
        Student student = studentRepository.findById(request.studentId()).orElseThrow(() -> new ResourceNotFoundException("Student", request.studentId()));
        Course course = courseRepository.findById(request.courseId()).orElseThrow(() -> new ResourceNotFoundException("Course", request.courseId()));
        // Business rule 1: only a student who follows (or followed) the course can review it
        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(student.getId(), course.getId(), EnrollmentStatus.CANCELLED);
        if (!enrolled) {
            throw new BusinessRuleException("A student must be enrolled in a course before reviewing it");
        }
        // Business rule 2: one review per student per course
        if (reviewRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new ConflictException(student.getFullName() + " has already reviewed \'" + course.getTitle() + "\'");
        }
        Review review = new Review();
        review.setStudent(student);
        review.setCourse(course);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Transactional
    public Review update(Long id, ReviewUpdateRequest request) {
        Review review = getById(id);
        review.setRating(request.rating());
        review.setComment(request.comment());
        return reviewRepository.save(review);
    }

    @Transactional
    public void delete(Long id) {
        reviewRepository.delete(getById(id));
    }

    public ReviewService(final ReviewRepository reviewRepository, final StudentRepository studentRepository, final CourseRepository courseRepository, final EnrollmentRepository enrollmentRepository) {
        this.reviewRepository = reviewRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }
}
