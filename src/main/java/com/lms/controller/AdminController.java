package com.lms.controller;

import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserRepository       userRepo;
    @Autowired private RoleRepository       roleRepo;
    @Autowired private CourseRepository     courseRepo;
    @Autowired private EnrollmentRepository enrollRepo;
    @Autowired private SubmissionRepository submissionRepo;
    @Autowired private PasswordEncoder      passwordEncoder;

    // GET /api/admin/users  — list all users
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<UserDTO> list = userRepo.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    // POST /api/admin/users  — create any user (including admin)
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AuthDTOs.RegisterRequest req) {

        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email already in use"));
        }

        String roleName = req.getRole() != null
                ? req.getRole().toUpperCase() : "STUDENT";

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .build();

        userRepo.save(user);
        return ResponseEntity.ok(ApiResponse.ok("User created", toDTO(user)));
    }

    // PUT /api/admin/users/{id}/toggle-status  — activate or deactivate user
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(!user.getIsActive());
        userRepo.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Status updated", toDTO(user)));
    }

    // DELETE /api/admin/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    // GET /api/admin/analytics  — platform statistics
    @GetMapping("/analytics")
    public ResponseEntity<?> analytics() {
        AnalyticsDTO dto = new AnalyticsDTO();
        dto.setTotalUsers(userRepo.count());
        dto.setTotalCourses(courseRepo.count());
        dto.setTotalEnrollments(enrollRepo.count());
        dto.setTotalSubmissions(submissionRepo.count());
        dto.setPublishedCourses(
                (long) courseRepo.findByStatus(Course.Status.PUBLISHED).size());
        dto.setActiveStudents(
                (long) userRepo.findByRoleName("STUDENT").stream()
                        .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                        .count());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    // ── Helper ────────────────────────────────────────────────────
    private UserDTO toDTO(User u) {
        return new UserDTO(
                u.getId(), u.getName(), u.getEmail(),
                u.getRole().getName(), u.getBio(),
                u.getProfilePic(), u.getIsActive(), u.getCreatedAt()
        );
    }
}
