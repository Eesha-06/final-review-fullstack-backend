package com.lms.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class EnrollmentDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseThumbnail;
    private Long studentId;
    private String studentName;
    private LocalDateTime enrolledAt;
    private Boolean completed;
    private Double progressPercent;
}
