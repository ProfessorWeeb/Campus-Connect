package com.campusconnect.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    private Long groupId;
    private String groupName;
    private String content;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;
}

