package com.campusconnect.repository;

import com.campusconnect.model.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    List<UserGroup> findByUserId(Long userId);
    List<UserGroup> findByGroupId(Long groupId);
    Optional<UserGroup> findByUserIdAndGroupId(Long userId, Long groupId);
    boolean existsByUserIdAndGroupId(Long userId, Long groupId);
}

