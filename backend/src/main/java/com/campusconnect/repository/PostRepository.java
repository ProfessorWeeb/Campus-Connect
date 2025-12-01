package com.campusconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campusconnect.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByGroupIdOrderByCreatedAtDesc(Long groupId);
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    // Search functionality
    List<Post> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);
    List<Post> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String content);
    
    // Combined search
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY p.createdAt DESC")
    List<Post> searchPosts(@org.springframework.data.repository.query.Param("query") String query);
    
    // Get all posts ordered by date
    List<Post> findAllByOrderByCreatedAtDesc();
}

