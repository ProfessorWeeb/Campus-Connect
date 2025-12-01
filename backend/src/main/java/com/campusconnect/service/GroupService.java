package com.campusconnect.service;

import java.time.LocalDateTime;
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
import com.campusconnect.model.SearchHistory;
import com.campusconnect.model.User;
import com.campusconnect.repository.GroupInvitationRepository;
import com.campusconnect.repository.GroupJoinRequestRepository;
import com.campusconnect.repository.GroupRepository;
import com.campusconnect.repository.SearchHistoryRepository;
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

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

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
     * - Groups they've joined (names, courses, topics)
     * - Groups they've created (names, courses, topics)
     * - User's courses/interests
     * - Search history (group names searched, courses searched)
     * - Collaborative filtering: Groups that members of user's groups are part of
     * 
     * OPTIMIZED: Uses database queries to filter groups instead of loading all groups into memory
     */
    public List<Group> getRecommendedGroups(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get recent search history (last 30 days) - wrap in try-catch to prevent errors from breaking recommendations
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<String> searchedGroupNames = new ArrayList<>();
        List<String> searchedCourseNames = new ArrayList<>();
        List<String> searchedCourseCodes = new ArrayList<>();
        List<String> searchedTopics = new ArrayList<>();
        List<String> generalSearches = new ArrayList<>();
        
        try {
            searchedGroupNames = searchHistoryRepository.findDistinctQueriesByType(
                userId, SearchHistory.SearchType.GROUP_NAME, thirtyDaysAgo);
            searchedCourseNames = searchHistoryRepository.findDistinctQueriesByType(
                userId, SearchHistory.SearchType.COURSE_NAME, thirtyDaysAgo);
            searchedCourseCodes = searchHistoryRepository.findDistinctQueriesByType(
                userId, SearchHistory.SearchType.COURSE_CODE, thirtyDaysAgo);
            searchedTopics = searchHistoryRepository.findDistinctQueriesByType(
                userId, SearchHistory.SearchType.TOPIC, thirtyDaysAgo);
            generalSearches = searchHistoryRepository.findDistinctQueriesByType(
                userId, SearchHistory.SearchType.GENERAL, thirtyDaysAgo);
        } catch (Exception e) {
            // If search history query fails, continue without search history (don't break recommendations)
            // This can happen if the search_history table doesn't exist yet or has schema issues
        }

        // Collect keywords from user's groups (only load what we need)
        Set<String> groupNames = new HashSet<>();
        Set<String> courseNames = new HashSet<>();
        Set<String> courseCodes = new HashSet<>();
        Set<String> topics = new HashSet<>();
        Set<Long> userGroupIds = new HashSet<>(); // For collaborative filtering
        Set<Character> courseCodeFirstChars = new HashSet<>(); // First character of course codes for prioritization
        Set<String> courseCodePrefixes = new HashSet<>(); // Course code prefixes (e.g., "MATH 4" from "MATH 4110")
        
        // Add search history to keywords
        searchedGroupNames.forEach(name -> groupNames.add(name.toLowerCase()));
        searchedCourseNames.forEach(name -> courseNames.add(name.toLowerCase()));
        searchedCourseCodes.forEach(code -> courseCodes.add(code.toLowerCase()));
        searchedTopics.forEach(topic -> topics.add(topic.toLowerCase()));
        // General searches could be group names, courses, or topics - add to all
        generalSearches.forEach(query -> {
            String lowerQuery = query.toLowerCase();
            groupNames.add(lowerQuery);
            courseNames.add(lowerQuery);
            topics.add(lowerQuery);
        });

        // Get user's groups and extract keywords
        List<Group> userGroups = getUserGroups(userId);
        List<Group> createdGroups = groupRepository.findByCreatorId(userId);
        
        // Extract keywords from joined groups
        userGroups.forEach(group -> {
            userGroupIds.add(group.getId());
            if (group.getName() != null) {
                groupNames.add(group.getName().toLowerCase());
            }
            if (group.getCourseName() != null) {
                courseNames.add(group.getCourseName().toLowerCase());
            }
            if (group.getCourseCode() != null) {
                String code = group.getCourseCode().toLowerCase().trim();
                courseCodes.add(code);
                // Extract first non-space character for prioritization
                if (!code.isEmpty()) {
                    // Find first non-whitespace character (handles edge cases)
                    for (int i = 0; i < code.length(); i++) {
                        char ch = code.charAt(i);
                        if (!Character.isWhitespace(ch)) {
                            courseCodeFirstChars.add(Character.toUpperCase(ch));
                            break;
                        }
                    }
                    // Extract course code prefix (e.g., "MATH 4" from "MATH 4110")
                    // Look for pattern: LETTERS + SPACE + DIGIT (e.g., "MATH 4", "CSCI 2")
                    int spaceIndex = code.indexOf(' ');
                    if (spaceIndex > 0 && spaceIndex < code.length() - 1) {
                        // Extract up to and including the first digit after the space
                        String prefix = code.substring(0, spaceIndex + 1);
                        // Find the first digit after the space
                        for (int i = spaceIndex + 1; i < code.length(); i++) {
                            char ch = code.charAt(i);
                            if (Character.isDigit(ch)) {
                                prefix += ch;
                                courseCodePrefixes.add(prefix); // e.g., "math 4"
                                break;
                            } else if (!Character.isWhitespace(ch)) {
                                // If we hit a non-digit, non-space character, stop
                                break;
                            }
                        }
                    }
                }
            }
            if (group.getTopic() != null) {
                topics.add(group.getTopic().toLowerCase());
            }
        });

        // Extract keywords from created groups
        createdGroups.forEach(group -> {
            userGroupIds.add(group.getId());
            if (group.getName() != null) {
                groupNames.add(group.getName().toLowerCase());
            }
            if (group.getCourseName() != null) {
                courseNames.add(group.getCourseName().toLowerCase());
            }
            if (group.getCourseCode() != null) {
                String code = group.getCourseCode().toLowerCase().trim();
                courseCodes.add(code);
                // Extract first non-space character for prioritization
                if (!code.isEmpty()) {
                    // Find first non-whitespace character (handles edge cases)
                    for (int i = 0; i < code.length(); i++) {
                        char ch = code.charAt(i);
                        if (!Character.isWhitespace(ch)) {
                            courseCodeFirstChars.add(Character.toUpperCase(ch));
                            break;
                        }
                    }
                    // Extract course code prefix (e.g., "MATH 4" from "MATH 4110")
                    // Look for pattern: LETTERS + SPACE + DIGIT (e.g., "MATH 4", "CSCI 2")
                    int spaceIndex = code.indexOf(' ');
                    if (spaceIndex > 0 && spaceIndex < code.length() - 1) {
                        // Extract up to and including the first digit after the space
                        String prefix = code.substring(0, spaceIndex + 1);
                        // Find the first digit after the space
                        for (int i = spaceIndex + 1; i < code.length(); i++) {
                            char ch = code.charAt(i);
                            if (Character.isDigit(ch)) {
                                prefix += ch;
                                courseCodePrefixes.add(prefix); // e.g., "math 4"
                                break;
                            } else if (!Character.isWhitespace(ch)) {
                                // If we hit a non-digit, non-space character, stop
                                break;
                            }
                        }
                    }
                }
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

        // OPTIMIZED: Query database for matching groups instead of loading all groups
        Set<Group> candidateGroups = new HashSet<>();
        
        // Only query if we have keywords to search for
        // Query by group names (from groups user has joined/created + search history)
        if (!groupNames.isEmpty()) {
            for (String groupName : groupNames) {
                List<Group> matches = groupRepository.findRecommendedByGroupName(userId, groupName);
                candidateGroups.addAll(matches);
            }
        }
        
        // Query by course names (from groups + user courses + search history)
        if (!courseNames.isEmpty()) {
            for (String courseName : courseNames) {
                List<Group> matches = groupRepository.findRecommendedByCourseName(userId, courseName);
                candidateGroups.addAll(matches);
            }
        }
        
        // Query by course codes (from groups + search history)
        if (!courseCodes.isEmpty()) {
            for (String courseCode : courseCodes) {
                List<Group> matches = groupRepository.findRecommendedByCourseCode(userId, courseCode);
                candidateGroups.addAll(matches);
            }
        }
        
        // Query by topics (from groups + search history)
        if (!topics.isEmpty()) {
            for (String topic : topics) {
                List<Group> matches = groupRepository.findRecommendedByTopic(userId, topic);
                candidateGroups.addAll(matches);
            }
        }
        
        // Collaborative filtering: Groups that members of user's groups are part of
        Map<Group, Boolean> collaborativeGroups = new HashMap<>(); // Track which groups came from collaborative filtering
        if (!userGroupIds.isEmpty()) {
            List<Group> collaborativeMatches = groupRepository.findRecommendedByCollaborativeFiltering(userId, new ArrayList<>(userGroupIds));
            candidateGroups.addAll(collaborativeMatches);
            // Mark these as collaborative filtering groups for scoring
            collaborativeMatches.forEach(g -> collaborativeGroups.put(g, true));
        }
        
        // If no candidate groups found from keyword matching, ALWAYS try general recommendations
        // This handles the case where user's groups don't have course names/codes/topics set
        if (candidateGroups.isEmpty()) {
            return getGeneralRecommendationsFallback(userId);
        }

        // Score only the candidate groups (much smaller set than all groups)
        Map<Group, Integer> groupScores = new HashMap<>();
        
        for (Group group : candidateGroups) {
            int score = 0;
            boolean isCollaborative = collaborativeGroups.getOrDefault(group, false);

            // HIGH WEIGHT: Collaborative filtering (groups that members of user's groups are in)
            if (isCollaborative) {
                score += 25; // Strong signal - people in your groups are in these groups
            }

            // HIGH WEIGHT: Search history matches (user has searched for similar terms)
            // Check if group name matches searched group names
            if (group.getName() != null) {
                String groupName = group.getName().toLowerCase();
                for (String searchedName : searchedGroupNames) {
                    if (groupName.contains(searchedName.toLowerCase()) || 
                        searchedName.toLowerCase().contains(groupName)) {
                        score += 18; // User searched for group names like this
                        break;
                    }
                }
            }
            
            // Check if course name matches searched course names
            if (group.getCourseName() != null) {
                String groupCourseName = group.getCourseName().toLowerCase();
                for (String searchedCourse : searchedCourseNames) {
                    if (groupCourseName.contains(searchedCourse.toLowerCase()) || 
                        searchedCourse.toLowerCase().contains(groupCourseName)) {
                        score += 18; // User searched for courses like this
                        break;
                    }
                }
            }
            
            // Check if course code matches searched course codes
            if (group.getCourseCode() != null) {
                String groupCourseCode = group.getCourseCode().toLowerCase();
                for (String searchedCode : searchedCourseCodes) {
                    if (groupCourseCode.equals(searchedCode.toLowerCase())) {
                        score += 20; // Exact match with searched course code
                        break;
                    } else if (groupCourseCode.contains(searchedCode.toLowerCase()) || 
                               searchedCode.toLowerCase().contains(groupCourseCode)) {
                        score += 15; // Partial match with searched course code
                        break;
                    }
                }
            }
            
            // Check if topic matches searched topics
            if (group.getTopic() != null) {
                String groupTopic = group.getTopic().toLowerCase();
                for (String searchedTopic : searchedTopics) {
                    if (groupTopic.contains(searchedTopic.toLowerCase()) || 
                        searchedTopic.toLowerCase().contains(groupTopic)) {
                        score += 12; // User searched for topics like this
                        break;
                    }
                }
            }
            
            // Check general searches (could match any field)
            if (!generalSearches.isEmpty()) {
                String searchQuery = generalSearches.get(0).toLowerCase(); // Use most recent
                if (group.getName() != null && group.getName().toLowerCase().contains(searchQuery)) {
                    score += 15;
                } else if (group.getCourseName() != null && group.getCourseName().toLowerCase().contains(searchQuery)) {
                    score += 15;
                } else if (group.getCourseCode() != null && group.getCourseCode().toLowerCase().contains(searchQuery)) {
                    score += 15;
                } else if (group.getTopic() != null && group.getTopic().toLowerCase().contains(searchQuery)) {
                    score += 12;
                }
            }

            // Score based on group name match (from groups user has joined/created)
            if (group.getName() != null) {
                String groupName = group.getName().toLowerCase();
                for (String userGroupName : groupNames) {
                    if (groupName.equals(userGroupName)) {
                        score += 12; // Exact match
                    } else if (groupName.contains(userGroupName) || userGroupName.contains(groupName)) {
                        score += 8; // Partial match
                    }
                }
            }

            // Score based on course name match
            if (group.getCourseName() != null) {
                String groupCourseName = group.getCourseName().toLowerCase();
                for (String courseName : courseNames) {
                    if (groupCourseName.equals(courseName)) {
                        score += 15; // Exact match
                    } else if (groupCourseName.contains(courseName) || courseName.contains(groupCourseName)) {
                        score += 10; // Partial match
                    }
                }
            }

            // Score based on course code match
            if (group.getCourseCode() != null) {
                String groupCourseCode = group.getCourseCode().toLowerCase().trim();
                
                // HIGHEST PRIORITY: Course code prefix match (e.g., "MATH 4" matches "MATH 4150", "MATH 4260")
                if (!groupCourseCode.isEmpty() && !courseCodePrefixes.isEmpty()) {
                    for (String prefix : courseCodePrefixes) {
                        if (groupCourseCode.startsWith(prefix)) {
                            score += 30; // Highest priority - same course code prefix (e.g., MATH 4xxx courses)
                            break; // Only count once
                        }
                    }
                }
                
                // HIGH PRIORITY: First non-space character match (same department/subject area)
                // Only apply if prefix match didn't already apply
                if (!groupCourseCode.isEmpty() && !courseCodeFirstChars.isEmpty()) {
                    // Find first non-whitespace character
                    for (int i = 0; i < groupCourseCode.length(); i++) {
                        char ch = groupCourseCode.charAt(i);
                        if (!Character.isWhitespace(ch)) {
                            char groupFirstChar = Character.toUpperCase(ch);
                            if (courseCodeFirstChars.contains(groupFirstChar)) {
                                score += 22; // High priority - same department (e.g., CSCI, MATH, ENGL)
                            }
                            break; // Only check the first non-space character
                        }
                    }
                }
                
                // Exact and partial matches
                for (String courseCode : courseCodes) {
                    if (groupCourseCode.equals(courseCode)) {
                        score += 20; // Exact match is worth more
                    } else if (groupCourseCode.contains(courseCode) || courseCode.contains(groupCourseCode)) {
                        score += 8; // Partial match
                    }
                }
            }

            // Score based on topic match
            if (group.getTopic() != null) {
                String groupTopic = group.getTopic().toLowerCase();
                for (String topic : topics) {
                    if (groupTopic.equals(topic)) {
                        score += 8; // Exact match
                    } else if (groupTopic.contains(topic) || topic.contains(groupTopic)) {
                        score += 5; // Partial match
                    }
                }
            }

            // Small bonus for groups with available spots (full groups are still recommended)
            // Full groups are included in recommendations - they just get a slightly lower score
            if (group.getMaxSize() != null && group.getMembers().size() < group.getMaxSize()) {
                score += 2;
            }
            // Note: Full groups (members.size() >= maxSize) are still included if they have score > 0

            if (score > 0) {
                groupScores.put(group, score);
            }
        }

        // Sort by score (descending) and return top 10
        List<Group> recommended = groupScores.entrySet().stream()
                .sorted(Map.Entry.<Group, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // FALLBACK: If no recommendations found, show general recommendations (recent public groups)
        if (recommended.isEmpty()) {
            return getGeneralRecommendationsFallback(userId);
        }
        
        return recommended;
    }

    /**
     * Fallback method to get general recommendations when no specific matches are found
     */
    private List<Group> getGeneralRecommendationsFallback(Long userId) {
        // Try the general recommendations query first
        try {
            List<Group> generalRecommendations = groupRepository.findGeneralRecommendations(userId);
            if (!generalRecommendations.isEmpty()) {
                return generalRecommendations.stream()
                        .limit(10)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // If query fails, continue to fallback
        }
        
        // Last resort: Get any public active groups (excluding user's own)
        // This bypasses the query and directly filters all groups
        try {
            List<Group> allGroups = groupRepository.findAll();
            List<Group> allPublicGroups = allGroups.stream()
                    .filter(g -> g != null)
                    .filter(g -> g.getStatus() == Group.GroupStatus.ACTIVE)
                    .filter(g -> g.getVisibility() == Group.GroupVisibility.PUBLIC)
                    .filter(g -> g.getCreator() != null && !g.getCreator().getId().equals(userId))
                    .filter(g -> g.getMembers().stream().noneMatch(m -> m != null && m.getId().equals(userId)))
                    .sorted((g1, g2) -> {
                        if (g1.getCreatedAt() == null || g2.getCreatedAt() == null) {
                            return 0;
                        }
                        return g2.getCreatedAt().compareTo(g1.getCreatedAt());
                    })
                    .limit(10)
                    .collect(Collectors.toList());
            
            if (!allPublicGroups.isEmpty()) {
                return allPublicGroups;
            }
            
            // If still empty, try without status/visibility filters (groups might not be set correctly)
            allPublicGroups = allGroups.stream()
                    .filter(g -> g != null)
                    .filter(g -> g.getCreator() != null && !g.getCreator().getId().equals(userId))
                    .filter(g -> g.getMembers().stream().noneMatch(m -> m != null && m.getId().equals(userId)))
                    .limit(10)
                    .collect(Collectors.toList());
            
            return allPublicGroups;
        } catch (Exception e) {
            // If all else fails, return empty list
            return new ArrayList<>();
        }
    }

    /**
     * Diagnostic method to debug recommendation issues
     */
    public Map<String, Object> getRecommendedGroupsDebug(Long userId) {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get all groups
            List<Group> allGroups = groupRepository.findAll();
            debug.put("totalGroupsInDatabase", allGroups.size());
            
            // Get user's groups
            List<Group> userGroups = getUserGroups(userId);
            List<Group> createdGroups = groupRepository.findByCreatorId(userId);
            debug.put("userJoinedGroups", userGroups.size());
            debug.put("userCreatedGroups", createdGroups.size());
            
            // Get groups user is NOT part of
            Set<Long> userGroupIds = new HashSet<>();
            userGroups.forEach(g -> userGroupIds.add(g.getId()));
            createdGroups.forEach(g -> userGroupIds.add(g.getId()));
            
            List<Group> notUserGroups = allGroups.stream()
                    .filter(g -> !userGroupIds.contains(g.getId()))
                    .collect(Collectors.toList());
            debug.put("groupsNotUserPartOf", notUserGroups.size());
            
            // Check status and visibility
            long activePublic = notUserGroups.stream()
                    .filter(g -> g.getStatus() == Group.GroupStatus.ACTIVE)
                    .filter(g -> g.getVisibility() == Group.GroupVisibility.PUBLIC)
                    .count();
            debug.put("activePublicGroupsNotUserPartOf", activePublic);
            
            // Try the general recommendations query
            try {
                List<Group> generalRecs = groupRepository.findGeneralRecommendations(userId);
                debug.put("generalRecommendationsQueryResult", generalRecs.size());
                if (!generalRecs.isEmpty()) {
                    debug.put("generalRecommendationsSample", generalRecs.stream()
                            .limit(3)
                            .map(g -> {
                                Map<String, Object> groupInfo = new HashMap<>();
                                groupInfo.put("id", g.getId());
                                groupInfo.put("name", g.getName() != null ? g.getName() : "null");
                                groupInfo.put("status", g.getStatus() != null ? g.getStatus().toString() : "null");
                                groupInfo.put("visibility", g.getVisibility() != null ? g.getVisibility().toString() : "null");
                                groupInfo.put("creatorId", g.getCreator() != null ? g.getCreator().getId() : "null");
                                return groupInfo;
                            })
                            .collect(Collectors.toList()));
                }
            } catch (Exception e) {
                debug.put("generalRecommendationsQueryError", e.getMessage());
            }
            
            // Try the fallback
            List<Group> fallback = getGeneralRecommendationsFallback(userId);
            debug.put("fallbackResult", fallback.size());
            if (!fallback.isEmpty()) {
                debug.put("fallbackSample", fallback.stream()
                        .limit(3)
                        .map(g -> {
                            Map<String, Object> groupInfo = new HashMap<>();
                            groupInfo.put("id", g.getId());
                            groupInfo.put("name", g.getName() != null ? g.getName() : "null");
                            groupInfo.put("status", g.getStatus() != null ? g.getStatus().toString() : "null");
                            groupInfo.put("visibility", g.getVisibility() != null ? g.getVisibility().toString() : "null");
                            return groupInfo;
                        })
                        .collect(Collectors.toList()));
            }
            
            // Get actual recommendations
            List<Group> recommendations = getRecommendedGroups(userId);
            debug.put("actualRecommendationsCount", recommendations.size());
            
            // Extract keywords from user's groups
            Set<String> courseNames = new HashSet<>();
            Set<String> courseCodes = new HashSet<>();
            userGroups.forEach(g -> {
                if (g.getCourseName() != null) courseNames.add(g.getCourseName());
                if (g.getCourseCode() != null) courseCodes.add(g.getCourseCode());
            });
            createdGroups.forEach(g -> {
                if (g.getCourseName() != null) courseNames.add(g.getCourseName());
                if (g.getCourseCode() != null) courseCodes.add(g.getCourseCode());
            });
            debug.put("extractedCourseNames", courseNames);
            debug.put("extractedCourseCodes", courseCodes);
            
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            debug.put("errorStackTrace", e.getClass().getName());
        }
        
        return debug;
    }

    /**
     * Track a user's search for recommendation purposes
     */
    @Transactional
    public void trackSearch(Long userId, String query, SearchHistory.SearchType searchType) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setUser(user);
        searchHistory.setQuery(query.trim());
        searchHistory.setSearchType(searchType);
        
        searchHistoryRepository.save(searchHistory);
    }
}

