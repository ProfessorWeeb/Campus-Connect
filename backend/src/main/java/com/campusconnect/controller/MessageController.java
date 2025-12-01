package com.campusconnect.controller;

import com.campusconnect.dto.MessageDTO;
import com.campusconnect.model.Message;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:3000")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @PostMapping("/direct")
    public ResponseEntity<MessageDTO> sendDirectMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long recipientId,
            @RequestParam String content) {
        Message message = messageService.sendDirectMessage(userPrincipal.getId(), recipientId, content);
        return ResponseEntity.ok(messageService.convertToDTO(message));
    }

    @PostMapping("/group")
    public ResponseEntity<MessageDTO> sendGroupMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long groupId,
            @RequestParam String content) {
        Message message = messageService.sendGroupMessage(userPrincipal.getId(), groupId, content);
        return ResponseEntity.ok(messageService.convertToDTO(message));
    }

    @GetMapping("/direct/{otherUserId}")
    public ResponseEntity<List<MessageDTO>> getDirectMessages(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long otherUserId) {
        List<MessageDTO> messages = messageService.getDirectMessages(userPrincipal.getId(), otherUserId).stream()
                .map(messageService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<MessageDTO>> getGroupMessages(@PathVariable Long groupId) {
        List<MessageDTO> messages = messageService.getGroupMessages(groupId).stream()
                .map(messageService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<MessageDTO>> getInbox(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<MessageDTO> messages = messageService.getInbox(userPrincipal.getId()).stream()
                .map(messageService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long messageId) {
        messageService.markAsRead(messageId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long count = messageService.getUnreadCount(userPrincipal.getId());
        return ResponseEntity.ok(count);
    }
}

