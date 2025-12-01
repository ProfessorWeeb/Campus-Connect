package com.campusconnect.repository;

import com.campusconnect.model.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    List<GroupInvitation> findByInvitedUserId(Long userId);
    List<GroupInvitation> findByGroupId(Long groupId);
    Optional<GroupInvitation> findByGroupIdAndInvitedUserId(Long groupId, Long userId);
    List<GroupInvitation> findByInvitedUserIdAndStatus(Long userId, GroupInvitation.InvitationStatus status);
}

