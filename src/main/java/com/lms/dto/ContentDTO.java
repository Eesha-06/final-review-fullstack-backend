package com.lms.dto;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor
public class ContentDTO {
    private Long id;
    private Long courseId;
    private String title;
    private String contentType;  // TEXT / VIDEO / PDF / LINK
    private String contentUrl;
    private String contentText;
    private Integer orderIndex;
    private Integer durationMin;
}
