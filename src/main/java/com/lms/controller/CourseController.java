package com.lms.controller;

import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.repository.*;
import com.lms.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CourseController {

    @Autowired private CourseRepository       courseRepo;
    @Autowired private CategoryRepository     categoryRepo;
    @Autowired private UserRepository         userRepo;
    @Autowired private EnrollmentRepository   enrollRepo;
    @Autowired private CourseContentRepository contentRepo;
    @Autowired private JwtUtils               jwtUtils;

    // ── PUBLIC ────────────────────────────────────────────────────

    // GET /api/courses/public?keyword=java&categoryId=1
    @GetMapping("/courses/public")
    public ResponseEntity<?> getPublishedCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {

        List<CourseDTO> courses = courseRepo.searchPublished(keyword, categoryId)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(courses));
    }

    // GET /api/courses/public/{id}
    @GetMapping("/courses/public/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        return courseRepo.findById(id)
                .map(c -> ResponseEntity.ok(ApiResponse.ok(toDTO(c))))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/courses/public/{id}/content
    @GetMapping("/courses/public/{id}/content")
    public ResponseEntity<?> getCourseContent(@PathVariable Long id) {
        List<ContentDTO> list = contentRepo.findByCourseIdOrderByOrderIndex(id)
                .stream().map(this::toContentDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // GET /api/courses/{courseId}/assignments  (used by student to view)
    @GetMapping("/courses/{courseId}/course-assignments")
    public ResponseEntity<?> getCourseAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }
    // ── INSTRUCTOR ────────────────────────────────────────────────

    // GET /api/instructor/courses
    @GetMapping("/instructor/courses")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<?> getMyCourses(
            @RequestHeader("Authorization") String authHeader) {

        String email = jwtUtils.getEmailFromToken(authHeader.substring(7));
        User instructor = userRepo.findByEmail(email).orElseThrow();

        List<CourseDTO> courses = courseRepo.findByInstructorId(instructor.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(courses));
    }

    // POST /api/instructor/courses
    @PostMapping("/instructor/courses")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<?> createCourse(@RequestBody CourseRequest req,
                                           @RequestHeader("Authorization") String authHeader) {

        String email = jwtUtils.getEmailFromToken(authHeader.substring(7));
        User instructor = userRepo.findByEmail(email).orElseThrow();

        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepo.findById(req.getCategoryId()).orElse(null);
        }

        Course course = Course.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .instructor(instructor)
                .category(category)
                .thumbnail(req.getThumbnail())
                .price(req.getPrice())
                .level(req.getLevel() != null
                        ? Course.Level.valueOf(req.getLevel()) : Course.Level.BEGINNER)
                .status(req.getStatus() != null
                        ? Course.Status.valueOf(req.getStatus()) : Course.Status.DRAFT)
                .build();

        courseRepo.save(course);
        return ResponseEntity.ok(ApiResponse.ok("Course created", toDTO(course)));
    }

    // PUT /api/instructor/courses/{id}
    @PutMapping("/instructor/courses/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<?> updateCourse(@PathVariable Long id,
                                           @RequestBody CourseRequest req) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setTitle(req.getTitle());
        course.setDescription(req.getDescription());
        course.setThumbnail(req.getThumbnail());
        if (req.getPrice() != null) course.setPrice(req.getPrice());
        if (req.getLevel()  != null) course.setLevel(Course.Level.valueOf(req.getLevel()));
        if (req.getStatus() != null) course.setStatus(Course.Status.valueOf(req.getStatus()));
        if (req.getCategoryId() != null) {
            categoryRepo.findById(req.getCategoryId()).ifPresent(course::setCategory);
        }

        courseRepo.save(course);
        return ResponseEntity.ok(ApiResponse.ok("Course updated", toDTO(course)));
    }

    // DELETE /api/instructor/courses/{id}
    @DeleteMapping("/instructor/courses/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        courseRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Course deleted", null));
    }

    // POST /api/instructor/courses/{courseId}/content
    @PostMapping("/instructor/courses/{courseId}/content")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','CONTENT_CREATOR','ADMIN')")
    public ResponseEntity<?> addContent(@PathVariable Long courseId,
                                         @RequestBody ContentDTO dto,
                                         @RequestHeader("Authorization") String authHeader) {

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        String email = jwtUtils.getEmailFromToken(authHeader.substring(7));
        User creator = userRepo.findByEmail(email).orElseThrow();

        CourseContent.ContentType type = dto.getContentType() != null
                ? CourseContent.ContentType.valueOf(dto.getContentType())
                : CourseContent.ContentType.TEXT;

        CourseContent content = CourseContent.builder()
                .course(course)
                .title(dto.getTitle())
                .contentType(type)
                .contentUrl(dto.getContentUrl())
                .contentText(dto.getContentText())
                .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 0)
                .durationMin(dto.getDurationMin() != null ? dto.getDurationMin() : 0)
                .createdBy(creator)
                .build();

        contentRepo.save(content);
        return ResponseEntity.ok(ApiResponse.ok("Content added", toContentDTO(content)));
    }

    // ── ADMIN ─────────────────────────────────────────────────────

    // GET /api/admin/courses
    @GetMapping("/admin/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCourses() {
        List<CourseDTO> courses = courseRepo.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(courses));
    }

    // ── HELPERS ───────────────────────────────────────────────────

    private CourseDTO toDTO(Course c) {
        CourseDTO dto = new CourseDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setInstructorId(c.getInstructor().getId());
        dto.setInstructorName(c.getInstructor().getName());
        if (c.getCategory() != null) {
            dto.setCategoryId(c.getCategory().getId());
            dto.setCategoryName(c.getCategory().getName());
        }
        dto.setThumbnail(c.getThumbnail());
        dto.setPrice(c.getPrice());
        dto.setLevel(c.getLevel().name());
        dto.setStatus(c.getStatus().name());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setEnrollmentCount(enrollRepo.countByCourseId(c.getId()));
        return dto;
    }

    private ContentDTO toContentDTO(CourseContent cc) {
        ContentDTO dto = new ContentDTO();
        dto.setId(cc.getId());
        dto.setCourseId(cc.getCourse().getId());
        dto.setTitle(cc.getTitle());
        dto.setContentType(cc.getContentType().name());
        dto.setContentUrl(cc.getContentUrl());
        dto.setContentText(cc.getContentText());
        dto.setOrderIndex(cc.getOrderIndex());
        dto.setDurationMin(cc.getDurationMin());
        return dto;
    }
}
