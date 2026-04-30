package com.lms.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class AssignmentDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Integer maxScore;
}
