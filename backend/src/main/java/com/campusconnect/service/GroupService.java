package com.campusconnect.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusconnect.dto.GroupDTO;
import com.campusconnect.model.Group;
import com.campusconnect.model.GroupInvitation;
import com.campusconnect.model.GroupJoinRequest;
import com.campusconnect.model.User;
import com.campusconnect.repository.GroupInvitationRepository;
import com.campusconnect.repository.GroupJoinRequestRepository;
import com.campusconnect.repository.GroupRepository;
import com.campusconnect.repository.UserRepository;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupJoinRequestRepository joinRequestRepository;

    @Autowired
    private GroupInvitationRepository invitationRepository;

    @Transactional
    public Group createGroup(Long creatorId, String name, String description, 
                            String courseName, String courseCode, String topic, Integer maxSize,
                            Group.GroupVisibility visibility, Boolean requiresInvite, List<Long> invitedUserIds) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setCourseName(courseName);
        group.setCourseCode(courseCode);
        group.setTopic(topic);
        group.setMaxSize(maxSize != null ? maxSize : 10);
        group.setCreator(creator);
        group.getMembers().add(creator);
        group.setVisibility(visibility != null ? visibility : Group.GroupVisibility.PUBLIC);
        group.setRequiresInvite(requiresInvite != null ? requiresInvite : false);

        Group savedGroup = groupRepository.save(group);

        // Send invitations to invited users
        if (invitedUserIds != null && !invitedUserIds.isEmpty()) {
            sendInvitations(savedGroup.getId(), creatorId, invitedUserIds, null);
        }

        return savedGroup;
    }

    /**
     * Send invitations to users for a group
     */
    @Transactional
    public List<GroupInvitation> sendInvitations(Long groupId, Long inviterId, List<Long> invitedUserIds, String message) {
        Group group = getGroupById(groupId);
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!group.getCreator().getId().equals(inviterId)) {
            throw new RuntimeException("Only the group creator can send invitations");
        }

        List<GroupInvitation> invitations = new ArrayList<>();

        for (Long userId : invitedUserIds) {
            // Skip if user is already a member
            User invitedUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            if (group.getMembers().contains(invitedUser)) {
                continue; // Skip if already a member
            }

            // Check if invitation already exists
            if (invitationRepository.findByGroupIdAndInvitedUserId(groupId, userId).isPresent()) {
                continue; // Skip if invitation already exists
            }

            GroupInvitation invitation = new GroupInvitation();
            invitation.setGroup(group);
            invitation.setInvitedUser(invitedUser);
            invitation.setInviter(inviter);
            invitation.setMessage(message);
            invitation.setStatus(GroupInvitation.InvitationStatus.PENDING);

            invitations.add(invitationRepository.save(invitation));
        }

        return invitations;
    }

    /**
     * Accept a group invitation
     */
    @Transactional
    public Group acceptInvitation(Long invitationId, Long userId) {
        GroupInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!invitation.getInvitedUser().getId().equals(userId)) {
            throw new RuntimeException("You can only accept your own invitations");
        }

        if (invitation.getStatus() != GroupInvitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is no longer pending");
        }

        Group group = invitation.getGroup();

        if (group.getMembers().size() >= group.getMaxSize()) {
            throw new RuntimeException("Group is full");
        }

        group.getMembers().add(invitation.getInvitedUser());
        invitation.setStatus(GroupInvitation.InvitationStatus.ACCEPTED);

        groupRepository.save(group);
        invitationRepository.save(invitation);

        return group;
    }

    /**
     * Reject a group invitation
     */
    @Transactional
    public void rejectInvitation(Long invitationId, Long userId) {
        GroupInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!invitation.getInvitedUser().getId().equals(userId)) {
            throw new RuntimeException("You can only reject your own invitations");
        }

        invitation.setStatus(GroupInvitation.InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
    }

    /**
     * Get all invitations for a user
     */
    public List<GroupInvitation> getUserInvitations(Long userId) {
        return invitationRepository.findByInvitedUserIdAndStatus(userId, GroupInvitation.InvitationStatus.PENDING);
    }

    public List<Group> getAllGroups() {
        // Only return public groups (private groups are hidden from general listing)
        return groupRepository.findAll().stream()
                .filter(group -> group.getVisibility() == Group.GroupVisibility.PUBLIC)
                .toList();
    }

    public List<Group> searchGroups(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllGroups();
        }
        // Only return public groups in search results
        return groupRepository.searchGroups(query).stream()
                .filter(group -> group.getVisibility() == Group.GroupVisibility.PUBLIC)
                .toList();
    }

    public List<Group> getGroupsByCourse(String courseName) {
        // Only return public groups
        return groupRepository.findByCourseNameContainingIgnoreCase(courseName).stream()
                .filter(group -> group.getVisibility() == Group.GroupVisibility.PUBLIC)
                .toList();
    }

    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    @Transactional
    public GroupJoinRequest requestToJoinGroup(Long groupId, Long userId, String message) {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (group.getMembers().contains(user)) {
            throw new RuntimeException("User is already a member");
        }

        if (joinRequestRepository.findByGroupIdAndUserId(groupId, userId).isPresent()) {
            throw new RuntimeException("Join request already exists");
        }

        GroupJoinRequest request = new GroupJoinRequest();
        request.setGroup(group);
        request.setUser(user);
        request.setMessage(message);
        request.setStatus(GroupJoinRequest.RequestStatus.PENDING);

        return joinRequestRepository.save(request);
    }

    /**
     * Directly join a group (only works if group allows direct joining)
     * 
     * Allowed combinations:
     * - PUBLIC + requiresInvite=false → Direct join allowed
     * - PRIVATE + requiresInvite=false → Direct join allowed (if user has link)
     * 
     * Not allowed:
     * - requiresInvite=true → Must use requestToJoinGroup instead
     */
    @Transactional
    public Group joinGroupDirectly(Long groupId, Long userId) {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (group.getMembers().contains(user)) {
            throw new RuntimeException("User is already a member");
        }

        if (group.getMembers().size() >= group.getMaxSize()) {
            throw new RuntimeException("Group is full");
        }

        // Check if group allows direct joining
        if (group.getRequiresInvite()) {
            throw new RuntimeException("This group requires an invitation or approval to join. Please send a join request.");
        }

        // Private groups can still allow direct join if requiresInvite is false
        // (user must have the direct link to the group)
        group.getMembers().add(user);
        return groupRepository.save(group);
    }

    @Transactional
    public void acceptJoinRequest(Long requestId, Long adminId) {
        GroupJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Join request not found"));

        Group group = request.getGroup();
        if (!group.getCreator().getId().equals(adminId)) {
            throw new RuntimeException("Only group creator can accept requests");
        }

        if (group.getMembers().size() >= group.getMaxSize()) {
            throw new RuntimeException("Group is full");
        }

        group.getMembers().add(request.getUser());
        request.setStatus(GroupJoinRequest.RequestStatus.ACCEPTED);
        groupRepository.save(group);
        joinRequestRepository.save(request);
    }

    @Transactional
    public void rejectJoinRequest(Long requestId, Long adminId) {
        GroupJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Join request not found"));

        Group group = request.getGroup();
        if (!group.getCreator().getId().equals(adminId)) {
            throw new RuntimeException("Only group creator can reject requests");
        }

        request.setStatus(GroupJoinRequest.RequestStatus.REJECTED);
        joinRequestRepository.save(request);
    }

    public List<GroupJoinRequest> getJoinRequestsForGroup(Long groupId) {
        return joinRequestRepository.findByGroupIdAndStatus(groupId, GroupJoinRequest.RequestStatus.PENDING);
    }

    public List<Group> getUserGroups(Long userId) {
        return groupRepository.findByMembersId(userId);
    }

    public List<Group> getUserCreatedGroups(Long userId) {
        return groupRepository.findByCreatorId(userId);
    }

    /**
     * Delete all groups from the database
     * This will cascade delete join requests, but we need to manually delete invitations
     */
    @Transactional
    public int deleteAllGroups() {
        // Get all groups first
        List<Group> allGroups = groupRepository.findAll();
        
        // Delete all group invitations first (since they reference groups)
        for (Group group : allGroups) {
            List<GroupInvitation> invitations = invitationRepository.findByGroupId(group.getId());
            invitationRepository.deleteAll(invitations);
        }
        
        // Delete all groups (this will cascade delete join requests due to orphanRemoval)
        groupRepository.deleteAll(allGroups);
        
        return allGroups.size();
    }

    /**
     * Leave a group
     */
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!group.getMembers().contains(user)) {
            throw new RuntimeException("User is not a member of this group");
        }

        // Creator cannot leave the group (they would need to delete it instead)
        if (group.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Group creator cannot leave the group. Delete the group instead if you want to remove it.");
        }

        group.getMembers().remove(user);
        groupRepository.save(group);
    }

    /**
     * Remove a member from a group (only creator can remove members)
     */
    @Transactional
    public void removeMember(Long groupId, Long adminId, Long memberIdToRemove) {
        Group group = getGroupById(groupId);
        
        if (!group.getCreator().getId().equals(adminId)) {
            throw new RuntimeException("Only the group creator can remove members");
        }

        User memberToRemove = userRepository.findById(memberIdToRemove)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!group.getMembers().contains(memberToRemove)) {
            throw new RuntimeException("User is not a member of this group");
        }

        // Cannot remove the creator
        if (group.getCreator().getId().equals(memberIdToRemove)) {
            throw new RuntimeException("Cannot remove the group creator");
        }

        group.getMembers().remove(memberToRemove);
        groupRepository.save(group);
    }

    /**
     * Admin utility: Fix a group by name (remove member and/or change privacy)
     */
    @Transactional
    public Map<String, Object> fixGroupByName(String groupName, Boolean removeOneMember, Boolean setOpenJoin) {
        // Find the group by name
        List<Group> groups = groupRepository.findAll().stream()
                .filter(g -> g.getName().contains(groupName) || 
                            (groupName.contains("CSCI 4463") && g.getCourseCode() != null && g.getCourseCode().contains("4463")))
                .collect(Collectors.toList());
        
        if (groups.isEmpty()) {
            throw new RuntimeException("Group not found: " + groupName);
        }
        
        if (groups.size() > 1) {
            throw new RuntimeException("Multiple groups found matching: " + groupName + ". Please be more specific.");
        }
        
        Group group = groups.get(0);
        Map<String, Object> result = new HashMap<>();
        result.put("groupName", group.getName());
        result.put("groupId", group.getId());
        result.put("actions", new ArrayList<String>());
        
        // Remove one member if requested
        if (removeOneMember && group.getMembers().size() > 1) {
            // Find a non-creator member to remove
            User memberToRemove = group.getMembers().stream()
                    .filter(m -> !m.getId().equals(group.getCreator().getId()))
                    .findFirst()
                    .orElse(null);
            
            if (memberToRemove != null) {
                group.getMembers().remove(memberToRemove);
                result.put("memberRemoved", memberToRemove.getUsername());
                ((List<String>) result.get("actions")).add("Removed member: " + memberToRemove.getUsername());
            } else {
                result.put("memberRemoved", "No non-creator members to remove");
            }
        }
        
        // Set to open join if requested
        if (setOpenJoin) {
            boolean wasInviteOnly = group.getRequiresInvite();
            group.setRequiresInvite(false);
            if (wasInviteOnly) {
                ((List<String>) result.get("actions")).add("Changed from invite-only to open join");
            }
        }
        
        groupRepository.save(group);
        result.put("success", true);
        result.put("currentMembers", group.getMembers().size());
        result.put("requiresInvite", group.getRequiresInvite());
        
        return result;
    }

    /**
     * Get a human-readable privacy description for a group
     */
    public String getPrivacyDescription(Group group) {
        if (group.getVisibility() == Group.GroupVisibility.PUBLIC) {
            if (group.getRequiresInvite()) {
                return "Public - Invite Only";
            } else {
                return "Public - Open Join";
            }
        } else { // PRIVATE
            if (group.getRequiresInvite()) {
                return "Private - Invite Only";
            } else {
                return "Private - Direct Join";
            }
        }
    }

    public GroupDTO convertToDTO(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setCourseName(group.getCourseName());
        dto.setCourseCode(group.getCourseCode());
        dto.setTopic(group.getTopic());
        dto.setMaxSize(group.getMaxSize());
        dto.setCreatorId(group.getCreator().getId());
        dto.setCreatorName(group.getCreator().getUsername());
        dto.setMemberIds(group.getMembers().stream().map(User::getId).collect(Collectors.toSet()));
        dto.setCurrentSize(group.getMembers().size());
        dto.setStatus(group.getStatus().name());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setVisibility(group.getVisibility().name());
        dto.setRequiresInvite(group.getRequiresInvite());
        return dto;
    }

    /**
     * Update group privacy settings
     */
    @Transactional
    public Group updateGroupPrivacy(Long groupId, Long creatorId, Group.GroupVisibility visibility, Boolean requiresInvite) {
        Group group = getGroupById(groupId);
        
        if (!group.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("Only the group creator can update privacy settings");
        }

        group.setVisibility(visibility != null ? visibility : group.getVisibility());
        group.setRequiresInvite(requiresInvite != null ? requiresInvite : group.getRequiresInvite());
        
        return groupRepository.save(group);
    }

    /**
     * Update group information (only creator can update)
     */
    @Transactional
    public Group updateGroup(Long groupId, Long creatorId, String name, String description,
                            String courseName, String courseCode, String topic, Integer maxSize,
                            Group.GroupVisibility visibility, Boolean requiresInvite) {
        Group group = getGroupById(groupId);
        
        if (!group.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("Only the group creator can update the group");
        }

        if (name != null && !name.trim().isEmpty()) {
            group.setName(name);
        }
        if (description != null) {
            group.setDescription(description);
        }
        if (courseName != null && !courseName.trim().isEmpty()) {
            group.setCourseName(courseName);
        }
        if (courseCode != null) {
            group.setCourseCode(courseCode);
        }
        if (topic != null) {
            group.setTopic(topic);
        }
        if (maxSize != null && maxSize > 0) {
            // Ensure maxSize is at least the current number of members
            if (maxSize < group.getMembers().size()) {
                throw new RuntimeException("Max size cannot be less than current number of members");
            }
            group.setMaxSize(maxSize);
        }
        if (visibility != null) {
            group.setVisibility(visibility);
        }
        if (requiresInvite != null) {
            group.setRequiresInvite(requiresInvite);
        }

        return groupRepository.save(group);
    }

    /**
     * Delete a group (only creator can delete)
     */
    @Transactional
    public void deleteGroup(Long groupId, Long creatorId) {
        Group group = getGroupById(groupId);
        
        if (!group.getCreator().getId().equals(creatorId)) {
            throw new RuntimeException("Only the group creator can delete the group");
        }

        // Delete all invitations for this group
        List<GroupInvitation> invitations = invitationRepository.findByGroupId(groupId);
        invitationRepository.deleteAll(invitations);

        // Delete the group (cascade will handle join requests and member relationships)
        groupRepository.delete(group);
    }

    /**
     * Get recommended groups for a user based on:
     * - Groups they've joined (similar courses/topics)
     * - Groups they've created (similar courses/topics)
     * - User's courses/interests
     */
    public List<Group> getRecommendedGroups(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get groups user is already part of (joined or created)
        List<Group> userGroups = getUserGroups(userId);
        List<Group> createdGroups = groupRepository.findByCreatorId(userId);
        Set<Long> excludedGroupIds = new HashSet<>();
        userGroups.forEach(g -> excludedGroupIds.add(g.getId()));
        createdGroups.forEach(g -> excludedGroupIds.add(g.getId()));

        // Collect keywords from user's groups
        Set<String> courseNames = new HashSet<>();
        Set<String> courseCodes = new HashSet<>();
        Set<String> topics = new HashSet<>();

        userGroups.forEach(group -> {
            if (group.getCourseName() != null) {
                courseNames.add(group.getCourseName().toLowerCase());
            }
            if (group.getCourseCode() != null) {
                courseCodes.add(group.getCourseCode().toLowerCase());
            }
            if (group.getTopic() != null) {
                topics.add(group.getTopic().toLowerCase());
            }
        });

        createdGroups.forEach(group -> {
            if (group.getCourseName() != null) {
                courseNames.add(group.getCourseName().toLowerCase());
            }
            if (group.getCourseCode() != null) {
                courseCodes.add(group.getCourseCode().toLowerCase());
            }
            if (group.getTopic() != null) {
                topics.add(group.getTopic().toLowerCase());
            }
        });

        // Add user's courses
        if (user.getCourses() != null) {
            user.getCourses().forEach(course -> {
                courseNames.add(course.toLowerCase());
            });
        }

        // Find matching groups
        Map<Group, Integer> groupScores = new HashMap<>();
        
        // Get all public groups
        List<Group> allPublicGroups = getAllGroups();
        
        for (Group group : allPublicGroups) {
            if (excludedGroupIds.contains(group.getId())) {
                continue; // Skip groups user is already in
            }

            int score = 0;

            // Score based on course name match
            if (group.getCourseName() != null) {
                String groupCourseName = group.getCourseName().toLowerCase();
                for (String courseName : courseNames) {
                    if (groupCourseName.contains(courseName) || courseName.contains(groupCourseName)) {
                        score += 10;
                    }
                }
            }

            // Score based on course code match
            if (group.getCourseCode() != null) {
                String groupCourseCode = group.getCourseCode().toLowerCase();
                for (String courseCode : courseCodes) {
                    if (groupCourseCode.equals(courseCode)) {
                        score += 15; // Exact match is worth more
                    } else if (groupCourseCode.contains(courseCode) || courseCode.contains(groupCourseCode)) {
                        score += 8;
                    }
                }
            }

            // Score based on topic match
            if (group.getTopic() != null) {
                String groupTopic = group.getTopic().toLowerCase();
                for (String topic : topics) {
                    if (groupTopic.contains(topic) || topic.contains(groupTopic)) {
                        score += 5;
                    }
                }
            }

            // Bonus for groups with available spots
            if (group.getMembers().size() < group.getMaxSize()) {
                score += 2;
            }

            if (score > 0) {
                groupScores.put(group, score);
            }
        }

        // Sort by score (descending) and return top 10
        return groupScores.entrySet().stream()
                .sorted(Map.Entry.<Group, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}

