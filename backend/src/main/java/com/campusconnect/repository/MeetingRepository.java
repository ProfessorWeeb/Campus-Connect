package com.campusconnect.repository;

import com.campusconnect.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByGroupId(Long groupId);
    List<Meeting> findByOrganizerId(Long organizerId);
}

