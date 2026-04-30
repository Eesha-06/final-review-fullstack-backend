package com.lms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// ── User DTO ──────────────────────────────────────
@Data @AllArgsConstructor @NoArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String bio;
    private String profilePic;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
