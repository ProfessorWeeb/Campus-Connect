package com.campusconnect.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusconnect.model.Course;
import com.campusconnect.model.Group;
import com.campusconnect.model.User;
import com.campusconnect.repository.CourseRepository;
import com.campusconnect.repository.GroupRepository;
import com.campusconnect.repository.UserRepository;

@Service
public class BotSeederService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private MessageService messageService;
    
    private static final String[] FIRST_NAMES = {
        "Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Avery", "Quinn",
        "Blake", "Cameron", "Dakota", "Emery", "Finley", "Harper", "Hayden", "Jamie",
        "Kai", "Logan", "Noah", "Parker", "Reese", "River", "Rowan", "Sage", "Skyler"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Wilson", "Anderson", "Thomas",
        "Taylor", "Moore", "Jackson", "Martin", "Lee", "Thompson", "White", "Harris"
    };
    
    private static final String[] GROUP_TOPICS = {
        "Study Group", "Homework Help", "Exam Prep", "Project Collaboration",
        "Lab Partners", "Discussion Group", "Review Session", "Practice Problems"
    };
    
    // Allowed departments and their corresponding majors
    private static final String[] ALLOWED_DEPARTMENTS = {"CSCI", "ITEC", "ENGR", "PHYS", "MATH"};
    private static final String[] MAJORS = {
        "Computer Science", "Information Technology", "Engineering", "Physics", "Mathematics"
    };
    
    private final Random random = new Random();
    
    @Transactional
    public BotSeedResult seedBots(int count, int groupsToCreate) {
        List<User> createdBots = new ArrayList<>();
        List<Group> createdGroups = new ArrayList<>();
        
        // Check if bots already exist
        long existingBotCount = userRepository.findAll().stream()
            .filter(User::getIsBot)
            .count();
        
        if (existingBotCount > 0) {
            throw new RuntimeException("Bots already exist. Delete existing bots first using /api/admin/bots/delete-all");
        }
        
        // Ensure courses are initialized
        if (courseRepository.count() == 0) {
            courseService.initializeCourses();
        }
        
        // Get all courses from allowed departments only
        List<Course> allAllowedCourses = new ArrayList<>();
        for (String dept : ALLOWED_DEPARTMENTS) {
            List<Course> deptCourses = courseRepository.findByDepartment(dept);
            allAllowedCourses.addAll(deptCourses);
        }
        
        if (allAllowedCourses.isEmpty()) {
            throw new RuntimeException("No courses found in allowed departments. Please initialize courses first using /api/courses/initialize");
        }
        
        // Create bot users with diverse majors and courses
        for (int i = 1; i <= count; i++) {
            // Assign major based on department distribution
            int majorIndex = i % MAJORS.length;
            String major = MAJORS[majorIndex];
            
            // Get courses for the bot's major department
            String department = ALLOWED_DEPARTMENTS[majorIndex];
            List<Course> deptCourses = courseRepository.findByDepartment(department);
            if (deptCourses.isEmpty()) {
                // Fallback to all allowed courses if department has no courses
                deptCourses = allAllowedCourses;
            }
            
            User bot = createBotUser(i, major, deptCourses);
            createdBots.add(bot);
        }
        
        // Create groups using courses from all allowed departments
        // Ensure we use different courses for variety
        Collections.shuffle(allAllowedCourses); // Randomize course selection
        int groupsCreated = 0;
        int courseIndex = 0;
        Set<String> usedCourseCodes = new HashSet<>(); // Track used courses to ensure variety
        
        for (int i = 0; i < Math.min(groupsToCreate, createdBots.size()); i++) {
            User botCreator = createdBots.get(i);
            
            // Find a course that hasn't been used yet (or cycle through if all used)
            Course course = null;
            int attempts = 0;
            while (course == null && attempts < allAllowedCourses.size()) {
                Course candidate = allAllowedCourses.get(courseIndex % allAllowedCourses.size());
                if (!usedCourseCodes.contains(candidate.getCode()) || usedCourseCodes.size() >= allAllowedCourses.size()) {
                    course = candidate;
                    usedCourseCodes.add(candidate.getCode());
                }
                courseIndex++;
                attempts++;
            }
            
            // Fallback if somehow no course found
            if (course == null) {
                course = allAllowedCourses.get(i % allAllowedCourses.size());
            }
            
            Group group = createBotGroup(botCreator, i + 1, course);
            createdGroups.add(group);
            groupsCreated++;
        }
        
        // Have bots join groups (not just the ones they created)
        // Each bot joins 1-3 random groups they didn't create
        for (User bot : createdBots) {
            int groupsToJoin = 1 + random.nextInt(3); // Join 1-3 groups
            List<Group> availableGroups = createdGroups.stream()
                .filter(g -> !g.getCreator().getId().equals(bot.getId())) // Not groups they created
                .filter(g -> !g.getMembers().contains(bot)) // Not already a member
                .filter(g -> g.getMembers().size() < g.getMaxSize()) // Group not full
                .collect(Collectors.toList());
            
            Collections.shuffle(availableGroups);
            int joined = 0;
            for (Group group : availableGroups) {
                if (joined >= groupsToJoin) break;
                if (group.getMembers().size() < group.getMaxSize()) {
                    group.getMembers().add(bot);
                    groupRepository.save(group);
                    joined++;
                }
            }
        }
        
        // Find the real user (non-bot user) to send messages to
        User realUser = null;
        // Try to find user by email first (your account)
        try {
            realUser = userRepository.findByEmail("daniel.underwood@mga.edu").orElse(null);
            if (realUser != null && realUser.getIsBot()) {
                realUser = null; // Don't use if it's a bot
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // If not found, get first real user
        if (realUser == null) {
            List<User> realUsers = userRepository.findAllRealUsers();
            if (!realUsers.isEmpty()) {
                realUser = realUsers.get(0);
            }
        }
        
        // Each bot sends 1-3 messages to other bots
        List<String> messageTemplates = List.of(
            "Hey! I saw you're interested in %s. Want to study together?",
            "Hi! I noticed we're both in %s. Let's connect!",
            "Hello! I'm looking for study partners for %s. Interested?",
            "Hey there! Want to form a study group for %s?",
            "Hi! I saw you're taking %s too. Let's help each other out!",
            "Hello! Are you looking for study partners for %s?",
            "Hey! I'm organizing a study session for %s. Want to join?",
            "Hi! I need help with %s. Can we study together?"
        );
        
        int messagesSent = 0;
        for (User bot : createdBots) {
            int messagesToSend = 1 + random.nextInt(3); // Send 1-3 messages
            List<User> otherBots = createdBots.stream()
                .filter(b -> !b.getId().equals(bot.getId()))
                .collect(Collectors.toList());
            Collections.shuffle(otherBots);
            
            int sent = 0;
            for (User recipient : otherBots) {
                if (sent >= messagesToSend) break;
                
                // Get a random course from the bot's courses or a generic message
                String courseName = bot.getCourses().isEmpty() 
                    ? "our classes" 
                    : bot.getCourses().iterator().next();
                
                String template = messageTemplates.get(random.nextInt(messageTemplates.size()));
                String content = String.format(template, courseName);
                
                try {
                    messageService.sendDirectMessage(bot.getId(), recipient.getId(), content);
                    messagesSent++;
                    sent++;
                } catch (Exception e) {
                    // Skip if message fails (e.g., user doesn't exist)
                }
            }
        }
        
        // The 25 group creators message the real user
        if (realUser != null) {
            List<String> creatorMessages = List.of(
                "Hi! I just created a study group. Would you like to join?",
                "Hello! I started a new study group and thought you might be interested.",
                "Hey! I created a group for %s. Want to check it out?",
                "Hi there! I just created a study group. Interested in joining?",
                "Hello! I started a new group and thought you might want to be part of it.",
                "Hey! I created a study group for %s. Would you like to join us?",
                "Hi! I just set up a new study group. Want to join?",
                "Hello! I created a group and thought you might be interested in joining."
            );
            
            int creatorMessagesSent = 0;
            for (int i = 0; i < Math.min(25, createdGroups.size()); i++) {
                User groupCreator = createdGroups.get(i).getCreator();
                if (groupCreator.getIsBot()) {
                    String courseName = createdGroups.get(i).getCourseName();
                    String template = creatorMessages.get(random.nextInt(creatorMessages.size()));
                    String content = template.contains("%s") 
                        ? String.format(template, courseName)
                        : template;
                    
                    try {
                        messageService.sendDirectMessage(groupCreator.getId(), realUser.getId(), content);
                        creatorMessagesSent++;
                    } catch (Exception e) {
                        // Skip if message fails
                    }
                }
            }
        }
        
        BotSeedResult result = new BotSeedResult();
        result.setBotsCreated(createdBots.size());
        result.setGroupsCreated(groupsCreated);
        result.setBotIds(createdBots.stream().map(User::getId).toList());
        result.setGroupIds(createdGroups.stream().map(Group::getId).toList());
        
        return result;
    }
    
    /**
     * Create groups and messages for existing bots
     * This method uses existing bots to create groups and send messages
     */
    @Transactional
    public BotSeedResult createGroupsAndMessagesForExistingBots(int groupsToCreate) {
        // Get all existing bot users
        List<User> bots = userRepository.findAll().stream()
            .filter(User::getIsBot)
            .collect(Collectors.toList());
        
        if (bots.isEmpty()) {
            throw new RuntimeException("No bot users found. Please seed bots first using /api/admin/bots/seed");
        }
        
        // Ensure courses are initialized
        if (courseRepository.count() == 0) {
            courseService.initializeCourses();
        }
        
        // Get all courses from allowed departments only
        List<Course> allAllowedCourses = new ArrayList<>();
        for (String dept : ALLOWED_DEPARTMENTS) {
            List<Course> deptCourses = courseRepository.findByDepartment(dept);
            allAllowedCourses.addAll(deptCourses);
        }
        
        if (allAllowedCourses.isEmpty()) {
            throw new RuntimeException("No courses found in allowed departments. Please initialize courses first using /api/courses/initialize");
        }
        
        // Create groups using courses from all allowed departments
        Collections.shuffle(allAllowedCourses);
        List<Group> createdGroups = new ArrayList<>();
        int courseIndex = 0;
        Set<String> usedCourseCodes = new HashSet<>();
        
        for (int i = 0; i < Math.min(groupsToCreate, bots.size()); i++) {
            User botCreator = bots.get(i % bots.size());
            
            // Find a course that hasn't been used yet
            Course course = null;
            int attempts = 0;
            while (course == null && attempts < allAllowedCourses.size()) {
                Course candidate = allAllowedCourses.get(courseIndex % allAllowedCourses.size());
                if (!usedCourseCodes.contains(candidate.getCode()) || usedCourseCodes.size() >= allAllowedCourses.size()) {
                    course = candidate;
                    usedCourseCodes.add(candidate.getCode());
                }
                courseIndex++;
                attempts++;
            }
            
            if (course == null) {
                course = allAllowedCourses.get(i % allAllowedCourses.size());
            }
            
            Group group = createBotGroup(botCreator, i + 1, course);
            createdGroups.add(group);
        }
        
        // Have bots join groups (not just the ones they created)
        for (User bot : bots) {
            int groupsToJoin = 1 + random.nextInt(3); // Join 1-3 groups
            List<Group> availableGroups = createdGroups.stream()
                .filter(g -> !g.getCreator().getId().equals(bot.getId()))
                .filter(g -> !g.getMembers().contains(bot))
                .filter(g -> g.getMembers().size() < g.getMaxSize())
                .collect(Collectors.toList());
            
            Collections.shuffle(availableGroups);
            int joined = 0;
            for (Group group : availableGroups) {
                if (joined >= groupsToJoin) break;
                if (group.getMembers().size() < group.getMaxSize()) {
                    group.getMembers().add(bot);
                    groupRepository.save(group);
                    joined++;
                }
            }
        }
        
        // Find the real user (non-bot user) to send messages to
        User realUser = null;
        try {
            realUser = userRepository.findByEmail("daniel.underwood@mga.edu").orElse(null);
            if (realUser != null && realUser.getIsBot()) {
                realUser = null;
            }
        } catch (Exception e) {
            // Ignore
        }
        
        if (realUser == null) {
            List<User> realUsers = userRepository.findAllRealUsers();
            if (!realUsers.isEmpty()) {
                realUser = realUsers.get(0);
            }
        }
        
        // Each bot sends 1-3 messages to other bots
        List<String> messageTemplates = List.of(
            "Hey! I saw you're interested in %s. Want to study together?",
            "Hi! I noticed we're both in %s. Let's connect!",
            "Hello! I'm looking for study partners for %s. Interested?",
            "Hey there! Want to form a study group for %s?",
            "Hi! I saw you're taking %s too. Let's help each other out!",
            "Hello! Are you looking for study partners for %s?",
            "Hey! I'm organizing a study session for %s. Want to join?",
            "Hi! I need help with %s. Can we study together?"
        );
        
        int messagesSent = 0;
        for (User bot : bots) {
            int messagesToSend = 1 + random.nextInt(3); // Send 1-3 messages
            List<User> otherBots = bots.stream()
                .filter(b -> !b.getId().equals(bot.getId()))
                .collect(Collectors.toList());
            Collections.shuffle(otherBots);
            
            int sent = 0;
            for (User recipient : otherBots) {
                if (sent >= messagesToSend) break;
                
                String courseName = bot.getCourses().isEmpty() 
                    ? "our classes" 
                    : bot.getCourses().iterator().next();
                
                String template = messageTemplates.get(random.nextInt(messageTemplates.size()));
                String content = String.format(template, courseName);
                
                try {
                    messageService.sendDirectMessage(bot.getId(), recipient.getId(), content);
                    messagesSent++;
                    sent++;
                } catch (Exception e) {
                    // Skip if message fails
                }
            }
        }
        
        // The group creators message the real user
        int creatorMessagesSent = 0;
        if (realUser != null) {
            List<String> creatorMessages = List.of(
                "Hi! I just created a study group. Would you like to join?",
                "Hello! I started a new study group and thought you might be interested.",
                "Hey! I created a group for %s. Want to check it out?",
                "Hi there! I just created a study group. Interested in joining?",
                "Hello! I started a new group and thought you might want to be part of it.",
                "Hey! I created a study group for %s. Would you like to join us?",
                "Hi! I just set up a new study group. Want to join?",
                "Hello! I created a group and thought you might be interested in joining."
            );
            
            for (int i = 0; i < Math.min(25, createdGroups.size()); i++) {
                User groupCreator = createdGroups.get(i).getCreator();
                if (groupCreator.getIsBot()) {
                    String courseName = createdGroups.get(i).getCourseName();
                    String template = creatorMessages.get(random.nextInt(creatorMessages.size()));
                    String content = template.contains("%s") 
                        ? String.format(template, courseName)
                        : template;
                    
                    try {
                        messageService.sendDirectMessage(groupCreator.getId(), realUser.getId(), content);
                        creatorMessagesSent++;
                    } catch (Exception e) {
                        // Skip if message fails
                    }
                }
            }
        }
        
        BotSeedResult result = new BotSeedResult();
        result.setBotsCreated(0); // No new bots created
        result.setGroupsCreated(createdGroups.size());
        result.setBotIds(bots.stream().map(User::getId).toList());
        result.setGroupIds(createdGroups.stream().map(Group::getId).toList());
        
        return result;
    }
    
    /**
     * Replace all existing bot groups with groups using CSCI courses
     */
    @Transactional
    public BotSeedResult replaceBotGroups() {
        // Get all bot users
        List<User> bots = userRepository.findAll().stream()
            .filter(User::getIsBot)
            .toList();
        
        if (bots.isEmpty()) {
            throw new RuntimeException("No bot users found. Please seed bots first using /api/admin/bots/seed");
        }
        
        // Ensure courses are initialized
        if (courseRepository.count() == 0) {
            courseService.initializeCourses();
        }
        
        // Get all CSCI courses
        List<Course> csciCourses = courseRepository.findByDepartment("CSCI");
        if (csciCourses.isEmpty()) {
            throw new RuntimeException("No CSCI courses found. Please initialize courses first using /api/courses/initialize");
        }
        
        // Delete all existing bot groups
        List<Group> existingGroups = new ArrayList<>();
        for (User bot : bots) {
            List<Group> botGroups = groupRepository.findByCreatorId(bot.getId());
            existingGroups.addAll(botGroups);
        }
        groupRepository.deleteAll(existingGroups);
        
        // Create new groups with CSCI courses
        List<Group> newGroups = new ArrayList<>();
        int groupIndex = 0;
        for (User bot : bots) {
            // Create 1-2 groups per bot, using different courses
            int groupsPerBot = 1 + random.nextInt(2);
            for (int i = 0; i < groupsPerBot && groupIndex < csciCourses.size() * 2; i++) {
                Course course = csciCourses.get(groupIndex % csciCourses.size());
                Group group = createBotGroup(bot, groupIndex + 1, course);
                newGroups.add(group);
                groupIndex++;
            }
        }
        
        BotSeedResult result = new BotSeedResult();
        result.setBotsCreated(0);
        result.setGroupsCreated(newGroups.size());
        result.setBotIds(List.of());
        result.setGroupIds(newGroups.stream().map(Group::getId).toList());
        
        return result;
    }
    
    private User createBotUser(int index, String major, List<Course> courses) {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String username = "bot_" + firstName.toLowerCase() + "_" + index;
        String email = "bot" + index + "@test.mga.edu";
        
        // Ensure unique username and email
        int attempt = 0;
        while (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            username = "bot_" + firstName.toLowerCase() + "_" + index + "_" + attempt;
            email = "bot" + index + "_" + attempt + "@test.mga.edu";
            attempt++;
        }
        
        User bot = new User();
        bot.setUsername(username);
        bot.setEmail(email);
        bot.setPassword(passwordEncoder.encode("botpassword123")); // All bots have same password
        bot.setFirstName(firstName);
        bot.setLastName(lastName);
        bot.setMajor(major);
        bot.setBio("This is a test bot account for API testing.");
        bot.setIsBot(true);
        bot.setRole(User.UserRole.STUDENT);
        bot.setVisibility(User.ProfileVisibility.PUBLIC);
        
        // Add interests based on major
        bot.getInterests().add("Study Groups");
        if (major.equals("Computer Science")) {
            bot.getInterests().add("Programming");
            if (random.nextBoolean()) bot.getInterests().add("Data Structures");
            if (random.nextBoolean()) bot.getInterests().add("Software Engineering");
        } else if (major.equals("Information Technology")) {
            bot.getInterests().add("IT Systems");
            if (random.nextBoolean()) bot.getInterests().add("Networking");
            if (random.nextBoolean()) bot.getInterests().add("Database Management");
        } else if (major.equals("Engineering")) {
            bot.getInterests().add("Engineering Design");
            if (random.nextBoolean()) bot.getInterests().add("Problem Solving");
        } else if (major.equals("Physics")) {
            bot.getInterests().add("Physics");
            if (random.nextBoolean()) bot.getInterests().add("Mathematics");
        } else if (major.equals("Mathematics")) {
            bot.getInterests().add("Mathematics");
            if (random.nextBoolean()) bot.getInterests().add("Statistics");
            if (random.nextBoolean()) bot.getInterests().add("Calculus");
        }
        
        // Add 2-4 random courses from the bot's department to each bot
        int numCourses = 2 + random.nextInt(3); // 2-4 courses
        List<Course> selectedCourses = new ArrayList<>(courses);
        Collections.shuffle(selectedCourses);
        for (int i = 0; i < numCourses && !selectedCourses.isEmpty(); i++) {
            Course course = selectedCourses.get(i);
            bot.getCourses().add(course.getCode());
        }
        
        return userRepository.save(bot);
    }
    
    private Group createBotGroup(User creator, int groupIndex, Course course) {
        String topic = GROUP_TOPICS[random.nextInt(GROUP_TOPICS.length)];
        
        String groupName = course.getCode() + " - " + topic;
        String description = "A test study group for " + course.getName() + " (" + course.getCode() + "). This is a bot-created group for testing purposes.";
        
        Group group = new Group();
        group.setName(groupName);
        group.setDescription(description);
        group.setCourseName(course.getName());
        group.setCourseCode(course.getCode());
        group.setTopic(topic);
        group.setMaxSize(5 + random.nextInt(6)); // Random between 5-10
        group.setCreator(creator);
        group.getMembers().add(creator);
        group.setStatus(Group.GroupStatus.ACTIVE);
        group.setVisibility(Group.GroupVisibility.PUBLIC);
        group.setRequiresInvite(random.nextBoolean()); // Random privacy setting
        
        return groupRepository.save(group);
    }
    
    /**
     * Add more bot users to existing bots
     * POST /api/admin/bots/add-more?count=150
     */
    @Transactional
    public BotSeedResult addMoreBots(int count) {
        List<User> createdBots = new ArrayList<>();
        
        // Get existing bot count to generate unique usernames/emails
        long existingBotCount = userRepository.findAll().stream()
            .filter(User::getIsBot)
            .count();
        
        // Ensure courses are initialized
        if (courseRepository.count() == 0) {
            courseService.initializeCourses();
        }
        
        // Get all courses from allowed departments
        List<Course> allAllowedCourses = new ArrayList<>();
        for (String dept : ALLOWED_DEPARTMENTS) {
            List<Course> deptCourses = courseRepository.findByDepartment(dept);
            allAllowedCourses.addAll(deptCourses);
        }
        
        if (allAllowedCourses.isEmpty()) {
            throw new RuntimeException("No courses found in allowed departments. Please initialize courses first using /api/courses/initialize");
        }
        
        // Create additional bot users
        for (int i = 1; i <= count; i++) {
            int botIndex = (int)existingBotCount + i;
            // Assign major based on department distribution
            int majorIndex = botIndex % MAJORS.length;
            String major = MAJORS[majorIndex];
            
            // Get courses for the bot's major department
            String department = ALLOWED_DEPARTMENTS[majorIndex];
            List<Course> deptCourses = courseRepository.findByDepartment(department);
            if (deptCourses.isEmpty()) {
                deptCourses = allAllowedCourses;
            }
            
            User bot = createBotUser(botIndex, major, deptCourses);
            createdBots.add(bot);
        }
        
        BotSeedResult result = new BotSeedResult();
        result.setBotsCreated(createdBots.size());
        result.setGroupsCreated(0);
        result.setBotIds(createdBots.stream().map(User::getId).toList());
        result.setGroupIds(new ArrayList<>());
        
        return result;
    }

    /**
     * Create additional CSCI groups that are public and open join
     * POST /api/admin/bots/create-csci-groups?count=25
     */
    @Transactional
    public BotSeedResult createCSCIGroups(int count) {
        List<Group> createdGroups = new ArrayList<>();
        
        // Get all existing bots
        List<User> existingBots = userRepository.findAll().stream()
            .filter(User::getIsBot)
            .collect(Collectors.toList());
        
        if (existingBots.isEmpty()) {
            throw new RuntimeException("No bot users found. Please seed bots first using /api/admin/bots/seed or /api/admin/bots/add-more");
        }
        
        // Ensure courses are initialized
        if (courseRepository.count() == 0) {
            courseService.initializeCourses();
        }
        
        // Get only CSCI courses
        List<Course> csciCourses = courseRepository.findByDepartment("CSCI");
        if (csciCourses.isEmpty()) {
            throw new RuntimeException("No CSCI courses found. Please initialize courses first using /api/courses/initialize");
        }
        
        // Shuffle courses and bots for variety
        Collections.shuffle(csciCourses);
        Collections.shuffle(existingBots);
        
        // Create groups
        int courseIndex = 0;
        int botIndex = 0;
        
        for (int i = 0; i < count; i++) {
            // Cycle through courses if we need more groups than courses
            Course course = csciCourses.get(courseIndex % csciCourses.size());
            courseIndex++;
            
            // Cycle through bots
            User botCreator = existingBots.get(botIndex % existingBots.size());
            botIndex++;
            
            // Create group with specific settings: CSCI, PUBLIC, open join
            Group group = createCSCIGroup(botCreator, i + 1, course);
            createdGroups.add(group);
        }
        
        BotSeedResult result = new BotSeedResult();
        result.setBotsCreated(0);
        result.setGroupsCreated(createdGroups.size());
        result.setBotIds(new ArrayList<>());
        result.setGroupIds(createdGroups.stream().map(Group::getId).toList());
        
        return result;
    }

    /**
     * Create a CSCI group that is public and open join
     */
    private Group createCSCIGroup(User creator, int groupIndex, Course course) {
        String topic = GROUP_TOPICS[random.nextInt(GROUP_TOPICS.length)];
        
        String groupName = course.getCode() + " - " + topic;
        String description = "A study group for " + course.getName() + " (" + course.getCode() + ").";
        
        Group group = new Group();
        group.setName(groupName);
        group.setDescription(description);
        group.setCourseName(course.getName());
        group.setCourseCode(course.getCode());
        group.setTopic(topic);
        group.setMaxSize(5 + random.nextInt(6)); // Random between 5-10
        group.setCreator(creator);
        group.getMembers().add(creator);
        group.setStatus(Group.GroupStatus.ACTIVE);
        group.setVisibility(Group.GroupVisibility.PUBLIC); // Always PUBLIC
        group.setRequiresInvite(false); // Always open join
        
        return groupRepository.save(group);
    }
    
    public static class BotSeedResult {
        private int botsCreated;
        private int groupsCreated;
        private List<Long> botIds;
        private List<Long> groupIds;
        
        public int getBotsCreated() { return botsCreated; }
        public void setBotsCreated(int botsCreated) { this.botsCreated = botsCreated; }
        
        public int getGroupsCreated() { return groupsCreated; }
        public void setGroupsCreated(int groupsCreated) { this.groupsCreated = groupsCreated; }
        
        public List<Long> getBotIds() { return botIds; }
        public void setBotIds(List<Long> botIds) { this.botIds = botIds; }
        
        public List<Long> getGroupIds() { return groupIds; }
        public void setGroupIds(List<Long> groupIds) { this.groupIds = groupIds; }
    }
}

