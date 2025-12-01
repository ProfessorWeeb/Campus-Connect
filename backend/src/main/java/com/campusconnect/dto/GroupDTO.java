package com.campusconnect.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class GroupDTO {
    private Long id;
    private String name;
    private String description;
    private String courseName;
    private String courseCode;
    private String topic;
    private Integer maxSize;
    private Long creatorId;
    private String creatorName;
    private Set<Long> memberIds;
    private Integer currentSize;
    private String status;
    private String visibility;
    private Boolean requiresInvite;
    private LocalDateTime createdAt;
}

