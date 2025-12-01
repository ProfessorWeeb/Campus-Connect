package com.campusconnect.repository;

import com.campusconnect.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByCourseNameContainingIgnoreCase(String courseName);
    List<Group> findByCourseCodeContainingIgnoreCase(String courseCode);
    List<Group> findByTopicContainingIgnoreCase(String topic);
    
    @Query("SELECT g FROM Group g WHERE g.status = 'ACTIVE' AND " +
           "(LOWER(g.courseName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.courseCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.topic) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Group> searchGroups(@Param("query") String query);
    
    List<Group> findByCreatorId(Long creatorId);
    List<Group> findByMembersId(Long memberId);
}

