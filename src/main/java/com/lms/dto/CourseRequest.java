package com.lms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// ── Course create/update request ──────────────────
@Data
public class CourseRequest {
    private String title;
    private String description;
    private Long categoryId;
    private String thumbnail;
    private BigDecimal price;
    private String level;   // BEGINNER / INTERMEDIATE / ADVANCED
    private String status;  // DRAFT / PUBLISHED / ARCHIVED
}
