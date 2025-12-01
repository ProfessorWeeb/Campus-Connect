package com.campusconnect.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusconnect.dto.GroupDTO;
import com.campusconnect.model.Group;
import com.campusconnect.model.GroupInvitation;
import com.campusconnect.model.GroupJoinRequest;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.GroupService;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:3000")
public class GroupController {
    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String courseName,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Integer maxSize,
            @RequestParam(required = false, defaultValue = "PUBLIC") String visibility,
            @RequestParam(required = false, defaultValue = "false") Boolean requiresInvite,
            @RequestParam(required = false) List<Long> invitedUserIds) {
        
        Group.GroupVisibility groupVisibility;
        try {
            groupVisibility = Group.GroupVisibility.valueOf(visibility.toUpperCase());
        } catch (IllegalArgumentException e) {
            groupVisibility = Group.GroupVisibility.PUBLIC;
        }
        
        // Ensure requiresInvite is not null
        if (requiresInvite == null) {
            requiresInvite = false;
        }
        
        Group group = groupService.createGroup(userPrincipal.getId(), name, description, 
                                               courseName, courseCode, topic, maxSize,
                                               groupVisibility, requiresInvite, invitedUserIds);
        return ResponseEntity.ok(groupService.convertToDTO(group));
    }

    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        List<GroupDTO> groups = groupService.getAllGroups().stream()
                .map(groupService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/search")
    public ResponseEntity<List<GroupDTO>> searchGroups(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String query) {
        
        // Track search if user is authenticated
        if (userPrincipal != null && query != null && !query.trim().isEmpty()) {
            groupService.trackSearch(userPrincipal.getId(), query, 
                com.campusconnect.model.SearchHistory.SearchType.GENERAL);
        }
        
        List<GroupDTO> groups = groupService.searchGroups(query).stream()
                .map(groupService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/by-course")
    public ResponseEntity<List<GroupDTO>> getGroupsByCourse(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String courseName) {
        
        // Track course search if user is authenticated
        if (userPrincipal != null && courseName != null && !courseName.trim().isEmpty()) {
            groupService.trackSearch(userPrincipal.getId(), courseName, 
                com.campusconnect.model.SearchHistory.SearchType.COURSE_NAME);
        }
        
        List<GroupDTO> groups = groupService.getGroupsByCourse(courseName).stream()
                .map(groupService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable Long id) {
        Group group = groupService.getGroupById(id);
        return ResponseEntity.ok(groupService.convertToDTO(group));
    }

    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupDTO>> getMyGroups(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<GroupDTO> groups = groupService.getUserGroups(userPrincipal.getId()).stream()
                .map(groupService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/my-created-groups")
    public ResponseEntity<List<GroupDTO>> getMyCreatedGroups(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<GroupDTO> groups = groupService.getUserCreatedGroups(userPrincipal.getId()).stream()
                .map(groupService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<GroupDTO>> getRecommendedGroups(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<GroupDTO> groups = groupService.getRecommendedGroups(userPrincipal.getId()).stream()
                .map(groupService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groups);
    }

    /**
     * Diagnostic endpoint to debug recommendation issues
     * GET /api/groups/recommended/debug?userId=1
     * GET /api/groups/recommended/debug?username=Danie_000
     * Can be called with or without authentication
     */
    @GetMapping(value = "/recommended/debug", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getRecommendedGroupsDebug(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username) {
        
        Long targetUserId = null;
        
        // Try to get userId from various sources
        if (userId != null) {
            targetUserId = userId;
        } else if (username != null && !username.isEmpty()) {
            // Look up user by username
            try {
                com.campusconnect.model.User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
                targetUserId = user.getId();
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "User not found with username: " + username);
                return ResponseEntity.badRequest().body(error);
            }
        } else if (userPrincipal != null) {
            targetUserId = userPrincipal.getId();
        }
        
        if (targetUserId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "userId or username parameter required, or user must be authenticated");
            error.put("example", "Use ?userId=1 or ?username=your_username");
            return ResponseEntity.badRequest().body(error);
        }
        
        return ResponseEntity.ok(groupService.getRecommendedGroupsDebug(targetUserId));
    }

    /**
     * Join a group - automatically handles direct join or join request based on group settings
     * 
     * Privacy combinations:
     * 1. PUBLIC + requiresInvite=false  → Direct join (immediate)
     * 2. PUBLIC + requiresInvite=true   → Join request (needs approval)
     * 3. PRIVATE + requiresInvite=false  → Direct join (if user has link)
     * 4. PRIVATE + requiresInvite=true   → Join request (needs approval)
     */
    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @RequestParam(required = false) String message) {
        
        Group group = groupService.getGroupById(groupId);
        
        // If group requires invite/approval, create a join request
        if (group.getRequiresInvite()) {
            GroupJoinRequest request = groupService.requestToJoinGroup(groupId, userPrincipal.getId(), message);
            return ResponseEntity.ok(request);
        }
        
        // Otherwise, try to join directly
        try {
            Group updatedGroup = groupService.joinGroupDirectly(groupId, userPrincipal.getId());
            return ResponseEntity.ok(groupService.convertToDTO(updatedGroup));
        } catch (RuntimeException e) {
            // If direct join fails for any reason, create a join request instead
            GroupJoinRequest request = groupService.requestToJoinGroup(groupId, userPrincipal.getId(), 
                message != null ? message : e.getMessage());
            return ResponseEntity.ok(request);
        }
    }
    
    @PostMapping("/{groupId}/request-join")
    public ResponseEntity<GroupJoinRequest> requestToJoinGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @RequestParam(required = false) String message) {
        GroupJoinRequest request = groupService.requestToJoinGroup(groupId, userPrincipal.getId(), message);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/join-requests/{requestId}/accept")
    public ResponseEntity<Void> acceptJoinRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long requestId) {
        groupService.acceptJoinRequest(requestId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/join-requests/{requestId}/reject")
    public ResponseEntity<Void> rejectJoinRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long requestId) {
        groupService.rejectJoinRequest(requestId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/join-requests")
    public ResponseEntity<List<GroupJoinRequest>> getJoinRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId) {
        List<GroupJoinRequest> requests = groupService.getJoinRequestsForGroup(groupId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Leave a group
     */
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId) {
        groupService.leaveGroup(groupId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Remove a member from a group (only creator can remove)
     */
    @PostMapping("/{groupId}/remove-member/{memberId}")
    public ResponseEntity<GroupDTO> removeMember(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        groupService.removeMember(groupId, userPrincipal.getId(), memberId);
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(groupService.convertToDTO(group));
    }

    /**
     * Update group privacy settings
     */
    @PostMapping("/{groupId}/privacy")
    public ResponseEntity<GroupDTO> updateGroupPrivacy(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) Boolean requiresInvite) {
        
        Group.GroupVisibility groupVisibility = null;
        if (visibility != null) {
            try {
                groupVisibility = Group.GroupVisibility.valueOf(visibility.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid visibility, will use existing value
            }
        }
        
        Group group = groupService.updateGroupPrivacy(groupId, userPrincipal.getId(), groupVisibility, requiresInvite);
        return ResponseEntity.ok(groupService.convertToDTO(group));
    }

    /**
     * Send invitations to users for a group
     */
    @PostMapping("/{groupId}/invite")
    public ResponseEntity<List<GroupInvitation>> sendInvitations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @RequestParam List<Long> invitedUserIds,
            @RequestParam(required = false) String message) {
        List<GroupInvitation> invitations = groupService.sendInvitations(
                groupId, userPrincipal.getId(), invitedUserIds, message);
        return ResponseEntity.ok(invitations);
    }

    /**
     * Accept a group invitation
     */
    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<GroupDTO> acceptInvitation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long invitationId) {
        Group group = groupService.acceptInvitation(invitationId, userPrincipal.getId());
        return ResponseEntity.ok(groupService.convertToDTO(group));
    }

    /**
     * Reject a group invitation
     */
    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<Void> rejectInvitation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long invitationId) {
        groupService.rejectInvitation(invitationId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Get all pending invitations for the current user
     */
    @GetMapping("/invitations/my-invitations")
    public ResponseEntity<List<GroupInvitation>> getMyInvitations(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<GroupInvitation> invitations = groupService.getUserInvitations(userPrincipal.getId());
        return ResponseEntity.ok(invitations);
    }

    /**
     * Update a group (only creator can update)
     * PUT /api/groups/{groupId}
     */
    @PostMapping("/{groupId}")
    public ResponseEntity<GroupDTO> updateGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Integer maxSize,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) Boolean requiresInvite) {
        
        Group.GroupVisibility groupVisibility = null;
        if (visibility != null) {
            try {
                groupVisibility = Group.GroupVisibility.valueOf(visibility.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid visibility value, will use existing value
            }
        }
        
        Group updatedGroup = groupService.updateGroup(groupId, userPrincipal.getId(), name, description,
                                                      courseName, courseCode, topic, maxSize,
                                                      groupVisibility, requiresInvite);
        return ResponseEntity.ok(groupService.convertToDTO(updatedGroup));
    }

    /**
     * Delete a group (only creator can delete)
     * DELETE /api/groups/{groupId}
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> deleteGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long groupId) {
        try {
            groupService.deleteGroup(groupId, userPrincipal.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Group deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Admin utility: Fix a specific group by name
     * POST /api/groups/admin/fix-group?groupName=CSCI 4463 - Review Session
     */
    @PostMapping("/admin/fix-group")
    public ResponseEntity<Map<String, Object>> fixGroup(
            @RequestParam String groupName,
            @RequestParam(required = false, defaultValue = "false") Boolean removeOneMember,
            @RequestParam(required = false, defaultValue = "false") Boolean setOpenJoin) {
        try {
            Map<String, Object> result = groupService.fixGroupByName(groupName, removeOneMember, setOpenJoin);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Delete all groups (Admin only - use with caution!)
     * DELETE /api/groups/delete-all
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<Map<String, Object>> deleteAllGroups() {
        try {
            int deletedCount = groupService.deleteAllGroups();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All groups deleted successfully");
            response.put("groupsDeleted", deletedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

