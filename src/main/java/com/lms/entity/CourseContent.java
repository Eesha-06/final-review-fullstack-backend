package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ContentType contentType = ContentType.TEXT;

    private String contentUrl;

    @Column(columnDefinition = "LONGTEXT")
    private String contentText;

    @Builder.Default
    private Integer orderIndex = 0;

    @Builder.Default
    private Integer durationMin = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ContentType {
        VIDEO, PDF, TEXT, LINK
    }
}
