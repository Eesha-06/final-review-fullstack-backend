package com.lms.controller;

import com.lms.dto.ApiResponse;
import com.lms.dto.EnrollmentDTO;
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
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired private EnrollmentRepository enrollRepo;
    @Autowired private CourseRepository     courseRepo;
    @Autowired private UserRepository       userRepo;
    @Autowired private JwtUtils             jwtUtils;

    // POST /api/enrollments/{courseId}  — student enrolls in a course
    @PostMapping("/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enroll(@PathVariable Long courseId,
                                     @RequestHeader("Authorization") String authHeader) {

        String email  = jwtUtils.getEmailFromToken(authHeader.substring(7));
        User student  = userRepo.findByEmail(email).orElseThrow();

        if (enrollRepo.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("You are already enrolled in this course"));
        }

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();

        enrollRepo.save(enrollment);
        return ResponseEntity.ok(ApiResponse.ok("Enrolled successfully", toDTO(enrollment)));
    }

    // GET /api/enrollments/my  — get all courses I'm enrolled in
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> myEnrollments(
            @RequestHeader("Authorization") String authHeader) {

        String email = jwtUtils.getEmailFromToken(authHeader.substring(7));
        User student = userRepo.findByEmail(email).orElseThrow();

        List<EnrollmentDTO> list = enrollRepo.findByStudentId(student.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // GET /api/enrollments/course/{courseId}  — instructor sees who enrolled
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<?> enrollmentsByCourse(@PathVariable Long courseId) {

        List<EnrollmentDTO> list = enrollRepo.findByCourseId(courseId)
                .stream().map(this::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // ── Helper ────────────────────────────────────────────────────
    private EnrollmentDTO toDTO(Enrollment e) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(e.getId());
        dto.setCourseId(e.getCourse().getId());
        dto.setCourseTitle(e.getCourse().getTitle());
        dto.setCourseThumbnail(e.getCourse().getThumbnail());
        dto.setStudentId(e.getStudent().getId());
        dto.setStudentName(e.getStudent().getName());
        dto.setEnrolledAt(e.getEnrolledAt());
        dto.setCompleted(e.getCompleted());
        dto.setProgressPercent(0.0); // extend later with progress tracking
        return dto;
    }
}
