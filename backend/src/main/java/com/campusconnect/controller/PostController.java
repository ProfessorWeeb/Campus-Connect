package com.campusconnect.controller;

import java.util.List;

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

import com.campusconnect.model.Post;
import com.campusconnect.security.UserPrincipal;
import com.campusconnect.service.PostService;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:3000")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long groupId,
            @RequestParam(required = false) String title,
            @RequestParam String content) {
        Post post = postService.createPost(userPrincipal.getId(), groupId, title, content);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Post>> getPostsByGroup(@PathVariable Long groupId) {
        List<Post> posts = postService.getPostsByGroup(groupId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Post>> getPostsByAuthor(@PathVariable Long authorId) {
        List<Post> posts = postService.getPostsByAuthor(authorId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        postService.deletePost(id, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam(required = false) String query) {
        List<Post> posts = postService.searchPosts(query);
        return ResponseEntity.ok(posts);
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<Post>> searchByTitle(@RequestParam String title) {
        List<Post> posts = postService.getPostsByTitle(title);
        return ResponseEntity.ok(posts);
    }
}

