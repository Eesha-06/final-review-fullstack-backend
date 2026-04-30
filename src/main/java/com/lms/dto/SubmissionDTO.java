package com.lms.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class SubmissionDTO {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentName;
    private String submissionText;
    private String fileUrl;
    private LocalDateTime submittedAt;
    private Integer score;
    private String feedback;
    private LocalDateTime gradedAt;
}
