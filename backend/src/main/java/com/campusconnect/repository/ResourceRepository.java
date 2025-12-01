package com.campusconnect.repository;

import com.campusconnect.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByGroupId(Long groupId);
    List<Resource> findByUploaderId(Long uploaderId);
}

