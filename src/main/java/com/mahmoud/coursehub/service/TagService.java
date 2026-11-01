package com.mahmoud.coursehub.service;

import com.mahmoud.coursehub.exception.ConflictException;
import com.mahmoud.coursehub.exception.ResourceNotFoundException;
import com.mahmoud.coursehub.model.Course;
import com.mahmoud.coursehub.model.Tag;
import com.mahmoud.coursehub.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tagRepository;

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    public Tag getById(Long id) {
        return tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }

    @Transactional
    public Tag create(Tag tag) {
        checkNameIsUnique(tag.getName(), null);
        tag.setId(null);
        return tagRepository.save(tag);
    }

    @Transactional
    public Tag update(Long id, Tag data) {
        Tag existing = getById(id);
        checkNameIsUnique(data.getName(), id);
        existing.setName(data.getName());
        return tagRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = getById(id);
        // Course owns the join table, so we first detach the tag from every course;
        // otherwise the database would refuse the delete (foreign key in course_tags).
        for (Course course : new ArrayList<>(tag.getCourses())) {
            course.getTags().remove(tag);
        }
        tagRepository.delete(tag);
    }

    private void checkNameIsUnique(String name, Long currentId) {
        tagRepository.findByNameIgnoreCase(name).ifPresent(other -> {
            if (!other.getId().equals(currentId)) {
                throw new ConflictException("A tag named \'" + name + "\' already exists");
            }
        });
    }

    public TagService(final TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
}
