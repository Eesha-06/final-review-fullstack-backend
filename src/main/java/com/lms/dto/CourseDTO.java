package com.lms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// ── Course response DTO ───────────────────────────
@Data @AllArgsConstructor @NoArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private Long instructorId;
    private String instructorName;
    private Long categoryId;
    private String categoryName;
    private String thumbnail;
    private BigDecimal price;
    private String level;
    private String status;
    private LocalDateTime createdAt;
    private Long enrollmentCount;
}
