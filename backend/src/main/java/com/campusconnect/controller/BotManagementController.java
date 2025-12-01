package com.campusconnect.controller;

import com.campusconnect.model.User;
import com.campusconnect.repository.GroupRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.BotSeederService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/bots")
@CrossOrigin(origins = "http://localhost:3000")
public class BotManagementController {
    
    @Autowired
    private BotSeederService botSeederService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    /**
     * Seed bot users for testing
     * POST /api/admin/bots/seed?count=100&groups=25
     */
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedBots(
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(defaultValue = "25") int groups) {
        
        try {
            BotSeederService.BotSeedResult result = botSeederService.seedBots(count, groups);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bots seeded successfully");
            response.put("botsCreated", result.getBotsCreated());
            response.put("groupsCreated", result.getGroupsCreated());
            response.put("botIds", result.getBotIds());
            response.put("groupIds", result.getGroupIds());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Create groups and messages for existing bots
     * POST /api/admin/bots/create-groups-messages?groups=25
     */
    @PostMapping("/create-groups-messages")
    public ResponseEntity<Map<String, Object>> createGroupsAndMessages(
            @RequestParam(defaultValue = "25") int groups) {
        
        try {
            BotSeederService.BotSeedResult result = botSeederService.createGroupsAndMessagesForExistingBots(groups);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Groups and messages created successfully for existing bots");
            response.put("botsUsed", result.getBotIds().size());
            response.put("groupsCreated", result.getGroupsCreated());
            response.put("groupIds", result.getGroupIds());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get all bot users
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBots() {
        List<User> bots = userRepository.findAll().stream()
            .filter(User::getIsBot)
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", bots.size());
        response.put("bots", bots.stream().map(bot -> {
            Map<String, Object> botInfo = new HashMap<>();
            botInfo.put("id", bot.getId());
            botInfo.put("username", bot.getUsername());
            botInfo.put("email", bot.getEmail());
            botInfo.put("firstName", bot.getFirstName());
            botInfo.put("lastName", bot.getLastName());
            botInfo.put("major", bot.getMajor());
            return botInfo;
        }).collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get count of bot users
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getBotCount() {
        long count = userRepository.findAll().stream()
            .filter(User::getIsBot)
            .count();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete all bot users and their groups
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<Map<String, Object>> deleteAllBots() {
        try {
            List<User> bots = userRepository.findAll().stream()
                .filter(User::getIsBot)
                .collect(Collectors.toList());
            
            // Delete groups created by bots
            for (User bot : bots) {
                List<com.campusconnect.model.Group> botGroups = groupRepository.findByCreatorId(bot.getId());
                groupRepository.deleteAll(botGroups);
            }
            
            // Delete bot users
            userRepository.deleteAll(bots);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All bots and their groups deleted successfully");
            response.put("botsDeleted", bots.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Hide all bot users (set visibility to private)
     */
    @PostMapping("/hide")
    public ResponseEntity<Map<String, Object>> hideAllBots() {
        try {
            List<User> bots = userRepository.findAll().stream()
                .filter(User::getIsBot)
                .collect(Collectors.toList());
            
            bots.forEach(bot -> bot.setVisibility(User.ProfileVisibility.PRIVATE));
            userRepository.saveAll(bots);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All bots hidden successfully");
            response.put("botsHidden", bots.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Show all bot users (set visibility to public)
     */
    @PostMapping("/show")
    public ResponseEntity<Map<String, Object>> showAllBots() {
        try {
            List<User> bots = userRepository.findAll().stream()
                .filter(User::getIsBot)
                .collect(Collectors.toList());
            
            bots.forEach(bot -> bot.setVisibility(User.ProfileVisibility.PUBLIC));
            userRepository.saveAll(bots);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All bots shown successfully");
            response.put("botsShown", bots.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Replace all bot groups with groups using CSCI courses
     * POST /api/admin/bots/replace-groups
     */
    @PostMapping("/replace-groups")
    public ResponseEntity<Map<String, Object>> replaceBotGroups() {
        try {
            BotSeederService.BotSeedResult result = botSeederService.replaceBotGroups();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bot groups replaced successfully with CSCI courses");
            response.put("groupsCreated", result.getGroupsCreated());
            response.put("groupIds", result.getGroupIds());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Add more bot users to existing bots
     * POST /api/admin/bots/add-more?count=150
     */
    @PostMapping("/add-more")
    public ResponseEntity<Map<String, Object>> addMoreBots(
            @RequestParam(defaultValue = "150") int count) {
        try {
            BotSeederService.BotSeedResult result = botSeederService.addMoreBots(count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Additional bots created successfully");
            response.put("botsCreated", result.getBotsCreated());
            response.put("botIds", result.getBotIds());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Create additional CSCI groups that are public and open join
     * POST /api/admin/bots/create-csci-groups?count=25
     */
    @PostMapping("/create-csci-groups")
    public ResponseEntity<Map<String, Object>> createCSCIGroups(
            @RequestParam(defaultValue = "25") int count) {
        try {
            BotSeederService.BotSeedResult result = botSeederService.createCSCIGroups(count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CSCI groups created successfully (Public, Open Join)");
            response.put("groupsCreated", result.getGroupsCreated());
            response.put("groupIds", result.getGroupIds());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

