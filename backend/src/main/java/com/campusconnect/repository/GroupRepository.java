package com.campusconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusconnect.model.Group;

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
    
    /**
     * Find public active groups that match course names (case-insensitive partial match)
     */
    @Query("SELECT DISTINCT g FROM Group g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND g.visibility = 'PUBLIC' " +
           "AND g.creator.id != :userId " +
           "AND g.id NOT IN (SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId) " +
           "AND g.id NOT IN (SELECT gc.id FROM Group gc WHERE gc.creator.id = :userId) " +
           "AND (LOWER(g.courseName) LIKE LOWER(CONCAT('%', :courseName, '%')))")
    List<Group> findRecommendedByCourseName(@Param("userId") Long userId, @Param("courseName") String courseName);
    
    /**
     * Find public active groups that match course codes (case-insensitive partial match)
     */
    @Query("SELECT DISTINCT g FROM Group g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND g.visibility = 'PUBLIC' " +
           "AND g.creator.id != :userId " +
           "AND g.id NOT IN (SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId) " +
           "AND g.id NOT IN (SELECT gc.id FROM Group gc WHERE gc.creator.id = :userId) " +
           "AND g.courseCode IS NOT NULL " +
           "AND (LOWER(g.courseCode) LIKE LOWER(CONCAT('%', :courseCode, '%')))")
    List<Group> findRecommendedByCourseCode(@Param("userId") Long userId, @Param("courseCode") String courseCode);
    
    /**
     * Find public active groups that match topics (case-insensitive partial match)
     */
    @Query("SELECT DISTINCT g FROM Group g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND g.visibility = 'PUBLIC' " +
           "AND g.creator.id != :userId " +
           "AND g.id NOT IN (SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId) " +
           "AND g.id NOT IN (SELECT gc.id FROM Group gc WHERE gc.creator.id = :userId) " +
           "AND g.topic IS NOT NULL " +
           "AND (LOWER(g.topic) LIKE LOWER(CONCAT('%', :topic, '%')))")
    List<Group> findRecommendedByTopic(@Param("userId") Long userId, @Param("topic") String topic);
    
    /**
     * Find public active groups that match group names (case-insensitive partial match)
     */
    @Query("SELECT DISTINCT g FROM Group g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND g.visibility = 'PUBLIC' " +
           "AND g.creator.id != :userId " +
           "AND g.id NOT IN (SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId) " +
           "AND g.id NOT IN (SELECT gc.id FROM Group gc WHERE gc.creator.id = :userId) " +
           "AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :groupName, '%')))")
    List<Group> findRecommendedByGroupName(@Param("userId") Long userId, @Param("groupName") String groupName);
    
    /**
     * Collaborative filtering: Find groups that members of user's groups are part of
     * This finds groups where at least one member is also a member of one of the user's groups
     */
    @Query("SELECT DISTINCT g FROM Group g " +
           "JOIN g.members m " +
           "WHERE g.status = 'ACTIVE' " +
           "AND g.visibility = 'PUBLIC' " +
           "AND g.creator.id != :userId " +
           "AND g.id NOT IN (SELECT gm.id FROM Group gm JOIN gm.members m2 WHERE m2.id = :userId) " +
           "AND g.id NOT IN (SELECT gc.id FROM Group gc WHERE gc.creator.id = :userId) " +
           "AND m.id IN (SELECT DISTINCT m3.id FROM Group userGroup JOIN userGroup.members m3 " +
           "              WHERE userGroup.id IN :userGroupIds AND m3.id != :userId)")
    List<Group> findRecommendedByCollaborativeFiltering(
        @Param("userId") Long userId, 
        @Param("userGroupIds") List<Long> userGroupIds
    );
    
    /**
     * Fallback: Get general recommendations - recent public active groups the user hasn't created or joined
     */
    @Query("SELECT DISTINCT g FROM Group g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND g.visibility = 'PUBLIC' " +
           "AND g.creator.id != :userId " +
           "AND g.id NOT IN (SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId) " +
           "AND g.id NOT IN (SELECT gc.id FROM Group gc WHERE gc.creator.id = :userId) " +
           "ORDER BY g.createdAt DESC")
    List<Group> findGeneralRecommendations(@Param("userId") Long userId);
}

