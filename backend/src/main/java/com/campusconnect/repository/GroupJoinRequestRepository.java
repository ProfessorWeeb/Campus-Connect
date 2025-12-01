package com.campusconnect.repository;

import com.campusconnect.model.GroupJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {
    List<GroupJoinRequest> findByGroupId(Long groupId);
    List<GroupJoinRequest> findByUserId(Long userId);
    Optional<GroupJoinRequest> findByGroupIdAndUserId(Long groupId, Long userId);
    List<GroupJoinRequest> findByGroupIdAndStatus(Long groupId, GroupJoinRequest.RequestStatus status);
}

