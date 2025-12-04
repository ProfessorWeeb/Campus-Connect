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
    
    @org.springframework.data.jpa.repository.Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :userId AND m.recipient.id = :otherUserId) OR " +
            "(m.sender.id = :otherUserId AND m.recipient.id = :userId) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversationMessages(Long userId, Long otherUserId);
    
    @org.springframework.data.jpa.repository.Query("SELECT m FROM Message m WHERE " +
            "m.recipient.id = :userId OR m.sender.id = :userId " +
            "ORDER BY m.createdAt DESC")
    List<Message> findAllUserMessages(Long userId);
}

