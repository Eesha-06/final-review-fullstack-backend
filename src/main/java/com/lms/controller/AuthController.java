package com.lms.controller;

import com.lms.dto.ApiResponse;
import com.lms.dto.AuthDTOs;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.RoleRepository;
import com.lms.repository.UserRepository;
import com.lms.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtils jwtUtils;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTOs.LoginRequest req) {

        // Find user by email
        User user = userRepo.findByEmail(req.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid email or password"));
        }

        // Check account is active
        if (!user.getIsActive()) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Your account has been disabled"));
        }

        // Check password
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid email or password"));
        }

        // Generate JWT
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().getName());

        return ResponseEntity.ok(ApiResponse.ok(
                new AuthDTOs.AuthResponse(
                        token,
                        user.getRole().getName(),
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                )
        ));
    }

    // POST /api/auth/register  (students and instructors can self-register)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDTOs.RegisterRequest req) {

        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is already registered"));
        }

        // Default role is STUDENT
        String roleName = (req.getRole() != null && !req.getRole().isBlank())
                ? req.getRole().toUpperCase()
                : "STUDENT";

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .build();

        userRepo.save(user);

        String token = jwtUtils.generateToken(user.getEmail(), role.getName());

        return ResponseEntity.ok(ApiResponse.ok("Registration successful",
                new AuthDTOs.AuthResponse(
                        token,
                        role.getName(),
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                )
        ));
    }
}
