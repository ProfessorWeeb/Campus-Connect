package com.campusconnect.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusconnect.model.SearchHistory;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    /**
     * Find recent search history for a user, ordered by most recent first
     */
    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(Long userId);
    
    /**
     * Find recent search history for a user within a time period
     */
    List<SearchHistory> findByUserIdAndSearchedAtAfterOrderBySearchedAtDesc(
        Long userId, 
        LocalDateTime after
    );
    
    /**
     * Find distinct search queries by type for a user (recent searches)
     * PostgreSQL requires ORDER BY columns to be in SELECT when using DISTINCT
     */
    @Query("SELECT sh.query FROM SearchHistory sh " +
           "WHERE sh.user.id = :userId " +
           "AND sh.searchType = :searchType " +
           "AND sh.searchedAt >= :after " +
           "GROUP BY sh.query, sh.searchedAt " +
           "ORDER BY MAX(sh.searchedAt) DESC")
    List<String> findDistinctQueriesByType(
        @Param("userId") Long userId,
        @Param("searchType") SearchHistory.SearchType searchType,
        @Param("after") LocalDateTime after
    );
    
    /**
     * Find all distinct search queries for a user (recent searches, last 30 days)
     * PostgreSQL requires ORDER BY columns to be in SELECT when using DISTINCT
     */
    @Query("SELECT sh.query FROM SearchHistory sh " +
           "WHERE sh.user.id = :userId " +
           "AND sh.searchedAt >= :after " +
           "GROUP BY sh.query, sh.searchedAt " +
           "ORDER BY MAX(sh.searchedAt) DESC")
    List<String> findDistinctRecentQueries(
        @Param("userId") Long userId,
        @Param("after") LocalDateTime after
    );
    
    /**
     * Count searches by type for a user
     */
    long countByUserIdAndSearchType(Long userId, SearchHistory.SearchType searchType);
}

