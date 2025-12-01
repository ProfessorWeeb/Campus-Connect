package com.campusconnect.repository;

import com.campusconnect.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    List<Message> findByGroupIdOrderByCreatedAtAsc(Long groupId);
    List<Message> findBySenderIdAndRecipientIdOrderByCreatedAtAsc(Long senderId, Long recipientId);
    Long countByRecipientIdAndIsReadFalse(Long recipientId);
}

