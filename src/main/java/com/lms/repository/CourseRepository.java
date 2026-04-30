package com.lms.repository;

import com.lms.entity.Course;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByStatus(Course.Status status);

    List<Course> findByInstructorId(Long instructorId);

    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' " +
           "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%',:keyword,'%'))) " +
           "AND (:categoryId IS NULL OR c.category.id = :categoryId)")
    List<Course> searchPublished(@Param("keyword") String keyword,
                                  @Param("categoryId") Long categoryId);
}
