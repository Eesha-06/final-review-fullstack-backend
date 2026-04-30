package com.lms.dto;

import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor
public class AnalyticsDTO {
    private Long totalUsers;
    private Long totalCourses;
    private Long totalEnrollments;
    private Long totalSubmissions;
    private Long publishedCourses;
    private Long activeStudents;
}
