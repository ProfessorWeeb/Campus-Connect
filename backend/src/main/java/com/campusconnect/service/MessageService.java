package com.campusconnect.service;

import com.campusconnect.dto.MessageDTO;
import com.campusconnect.model.Group;
import com.campusconnect.model.Message;
import com.campusconnect.model.User;
import com.campusconnect.repository.GroupRepository;
import com.campusconnect.repository.MessageRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Transactional
    public Message sendDirectMessage(Long senderId, String recipientUsername, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new RuntimeException("Recipient not found with username: " + recipientUsername));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setType(Message.MessageType.DIRECT);
        message.setIsRead(false);

        return messageRepository.save(message);
    }

    @Transactional
    public Message sendGroupMessage(Long senderId, Long groupId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getMembers().contains(sender)) {
            throw new RuntimeException("User is not a member of this group");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setGroup(group);
        message.setContent(content);
        message.setType(Message.MessageType.GROUP);
        message.setIsRead(false);

        return messageRepository.save(message);
    }

    public List<Message> getDirectMessages(Long userId, Long otherUserId) {
        return messageRepository.findConversationMessages(userId, otherUserId);
    }

    public List<Message> getGroupMessages(Long groupId) {
        return messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId);
    }

    public List<Message> getInbox(Long userId) {
        // Return both sent and received messages for the user
        return messageRepository.findAllUserMessages(userId);
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getRecipient() != null && message.getRecipient().getId().equals(userId)) {
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }

    public Long getUnreadCount(Long userId) {
        return messageRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    public MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getUsername());
        if (message.getRecipient() != null) {
            dto.setRecipientId(message.getRecipient().getId());
            dto.setRecipientName(message.getRecipient().getUsername());
        }
        if (message.getGroup() != null) {
            dto.setGroupId(message.getGroup().getId());
            dto.setGroupName(message.getGroup().getName());
        }
        dto.setContent(message.getContent());
        dto.setType(message.getType().name());
        dto.setIsRead(message.getIsRead());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}

