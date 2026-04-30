package com.lms.controller;

import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.repository.*;
import com.lms.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AssignmentController {

    @Autowired private AssignmentRepository  assignmentRepo;
    @Autowired private SubmissionRepository  submissionRepo;
    @Autowired private CourseRepository      courseRepo;
    @Autowired private UserRepository        userRepo;
    @Autowired private JwtUtils              jwtUtils;

    // ── INSTRUCTOR: Create assignment ─────────────────────────────

    // POST /api/instructor/assignments
    @PostMapping("/instructor/assignments")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<?> createAssignment(@RequestBody AssignmentDTO dto,
                                               @RequestHeader("Authorization") String h) {

        String email = jwtUtils.getEmailFromToken(h.substring(7));
        User instructor = userRepo.findByEmail(email).orElseThrow();

        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Assignment assignment = Assignment.builder()
                .course(course)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .maxScore(dto.getMaxScore() != null ? dto.getMaxScore() : 100)
                .createdBy(instructor)
                .build();

        assignmentRepo.save(assignment);
        return ResponseEntity.ok(ApiResponse.ok("Assignment created", toDTO(assignment)));
    }

    // ── PUBLIC: Get assignments for a course ──────────────────────

    // GET /api/courses/{courseId}/assignments
    @GetMapping("/courses/{courseId}/assignments")
    public ResponseEntity<?> getCourseAssignments(@PathVariable Long courseId) {
        List<AssignmentDTO> list = assignmentRepo.findByCourseId(courseId)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // ── STUDENT: Submit an assignment ─────────────────────────────

    // POST /api/student/assignments/{assignmentId}/submit
    @PostMapping("/student/assignments/{assignmentId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submit(@PathVariable Long assignmentId,
                                     @RequestBody SubmissionDTO dto,
                                     @RequestHeader("Authorization") String h) {

        String email  = jwtUtils.getEmailFromToken(h.substring(7));
        User student  = userRepo.findByEmail(email).orElseThrow();

        // Check for duplicate submission
        if (submissionRepo.findByAssignmentIdAndStudentId(assignmentId, student.getId()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("You have already submitted this assignment"));
        }

        Assignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .submissionText(dto.getSubmissionText())
                .fileUrl(dto.getFileUrl())
                .build();

        submissionRepo.save(submission);
        return ResponseEntity.ok(ApiResponse.ok("Submitted successfully", toSubDTO(submission)));
    }

    // GET /api/student/submissions  — student views their own submissions
    @GetMapping("/student/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> mySubmissions(@RequestHeader("Authorization") String h) {
        String email = jwtUtils.getEmailFromToken(h.substring(7));
        User student = userRepo.findByEmail(email).orElseThrow();

        List<SubmissionDTO> list = submissionRepo.findByStudentId(student.getId())
                .stream().map(this::toSubDTO).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // ── INSTRUCTOR: View submissions ──────────────────────────────

    // GET /api/instructor/assignments/{assignmentId}/submissions
    @GetMapping("/instructor/assignments/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<?> getSubmissions(@PathVariable Long assignmentId) {
        List<SubmissionDTO> list = submissionRepo.findByAssignmentId(assignmentId)
                .stream().map(this::toSubDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // ── INSTRUCTOR: Grade a submission ────────────────────────────

    // PUT /api/instructor/submissions/{submissionId}/grade
    @PutMapping("/instructor/submissions/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    public ResponseEntity<?> grade(@PathVariable Long submissionId,
                                    @RequestBody GradeRequest req,
                                    @RequestHeader("Authorization") String h) {

        String email = jwtUtils.getEmailFromToken(h.substring(7));
        User instructor = userRepo.findByEmail(email).orElseThrow();

        Submission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        submission.setScore(req.getScore());
        submission.setFeedback(req.getFeedback());
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(instructor);

        submissionRepo.save(submission);
        return ResponseEntity.ok(ApiResponse.ok("Graded successfully", toSubDTO(submission)));
    }

    // ── HELPERS ───────────────────────────────────────────────────

    private AssignmentDTO toDTO(Assignment a) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(a.getId());
        dto.setCourseId(a.getCourse().getId());
        dto.setCourseTitle(a.getCourse().getTitle());
        dto.setTitle(a.getTitle());
        dto.setDescription(a.getDescription());
        dto.setDueDate(a.getDueDate());
        dto.setMaxScore(a.getMaxScore());
        return dto;
    }

    private SubmissionDTO toSubDTO(Submission s) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(s.getId());
        dto.setAssignmentId(s.getAssignment().getId());
        dto.setAssignmentTitle(s.getAssignment().getTitle());
        dto.setStudentId(s.getStudent().getId());
        dto.setStudentName(s.getStudent().getName());
        dto.setSubmissionText(s.getSubmissionText());
        dto.setFileUrl(s.getFileUrl());
        dto.setSubmittedAt(s.getSubmittedAt());
        dto.setScore(s.getScore());
        dto.setFeedback(s.getFeedback());
        dto.setGradedAt(s.getGradedAt());
        return dto;
    }
}
