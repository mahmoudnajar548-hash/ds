package com.mahmoud.coursehub.service;

import com.mahmoud.coursehub.dto.CoursePopularity;
import com.mahmoud.coursehub.dto.CourseRating;
import com.mahmoud.coursehub.dto.CourseRequest;
import com.mahmoud.coursehub.exception.ConflictException;
import com.mahmoud.coursehub.exception.ResourceNotFoundException;
import com.mahmoud.coursehub.model.Category;
import com.mahmoud.coursehub.model.Course;
import com.mahmoud.coursehub.model.EnrollmentStatus;
import com.mahmoud.coursehub.model.Instructor;
import com.mahmoud.coursehub.model.Student;
import com.mahmoud.coursehub.model.Tag;
import com.mahmoud.coursehub.repository.CategoryRepository;
import com.mahmoud.coursehub.repository.CourseRepository;
import com.mahmoud.coursehub.repository.EnrollmentRepository;
import com.mahmoud.coursehub.repository.InstructorRepository;
import com.mahmoud.coursehub.repository.ReviewRepository;
import com.mahmoud.coursehub.repository.StudentRepository;
import com.mahmoud.coursehub.repository.TagRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final InstructorRepository instructorRepository;
    private final TagRepository tagRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;

    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    public Course getById(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    @Transactional
    public Course create(CourseRequest request) {
        Course course = new Course();
        applyRequest(course, request);
        return courseRepository.save(course);
    }

    @Transactional
    public Course update(Long id, CourseRequest request) {
        Course course = getById(id);
        applyRequest(course, request);
        return courseRepository.save(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = getById(id);
        if (enrollmentRepository.existsByCourseId(id) || reviewRepository.existsByCourseId(id)) {
            throw new ConflictException("Cannot delete course \'" + course.getTitle() + "\': it still has enrollments or reviews");
        }
        courseRepository.delete(course);
    }

    // ---------- business queries ----------
    public List<Course> getByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
        return courseRepository.findByCategoryId(categoryId);
    }

    public List<Course> search(String keyword, BigDecimal maxPrice) {
        String cleanKeyword = keyword == null ? "" : keyword.trim();
        BigDecimal budget = maxPrice == null ? new BigDecimal("1000000") : maxPrice;
        return courseRepository.search(cleanKeyword, budget);
    }

    public List<CoursePopularity> getMostPopular(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50)); // keep between 1 and 50
        return enrollmentRepository.findMostPopularCourses(EnrollmentStatus.CANCELLED, PageRequest.of(0, safeLimit));
    }

    public List<Student> getStudents(Long courseId) {
        getById(courseId); // 404 if the course does not exist
        return studentRepository.findStudentsOfCourse(courseId, EnrollmentStatus.CANCELLED);
    }

    public CourseRating getRating(Long courseId) {
        Course course = getById(courseId);
        Double average = reviewRepository.findAverageRatingByCourseId(courseId);
        Double rounded = average == null ? null : Math.round(average * 100.0) / 100.0;
        return new CourseRating(course.getId(), course.getTitle(), rounded, reviewRepository.countByCourseId(courseId));
    }

    // ---------- helpers ----------
    // Copies the request into the entity, resolving ids into real entities (404 if one is unknown)
    private void applyRequest(Course course, CourseRequest request) {
        Category category = categoryRepository.findById(request.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
        Instructor instructor = instructorRepository.findById(request.instructorId()).orElseThrow(() -> new ResourceNotFoundException("Instructor", request.instructorId()));
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setPrice(request.price());
        course.setDurationHours(request.durationHours());
        course.setLevel(request.level());
        course.setCategory(category);
        course.setInstructor(instructor);
        course.setTags(resolveTags(request.tagIds()));
    }

    private Set<Tag> resolveTags(Set<Long> tagIds) {
        Set<Tag> tags = new HashSet<>();
        if (tagIds == null || tagIds.isEmpty()) {
            return tags;
        }
        List<Tag> found = tagRepository.findAllById(tagIds);
        Set<Long> foundIds = found.stream().map(Tag::getId).collect(Collectors.toSet());
        for (Long tagId : tagIds) {
            if (!foundIds.contains(tagId)) {
                throw new ResourceNotFoundException("Tag", tagId);
            }
        }
        tags.addAll(found);
        return tags;
    }

    public CourseService(final CourseRepository courseRepository, final CategoryRepository categoryRepository, final InstructorRepository instructorRepository, final TagRepository tagRepository, final StudentRepository studentRepository, final EnrollmentRepository enrollmentRepository, final ReviewRepository reviewRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.instructorRepository = instructorRepository;
        this.tagRepository = tagRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.reviewRepository = reviewRepository;
    }
}
