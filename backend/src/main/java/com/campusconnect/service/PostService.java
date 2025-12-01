package com.campusconnect.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusconnect.model.Group;
import com.campusconnect.model.Post;
import com.campusconnect.model.User;
import com.campusconnect.repository.GroupRepository;
import com.campusconnect.repository.PostRepository;
import com.campusconnect.repository.UserRepository;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Transactional
    public Post createPost(Long authorId, Long groupId, String title, String content) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Post post = new Post();
        post.setAuthor(author);
        post.setGroup(group);
        post.setTitle(title);
        post.setContent(content);

        return postRepository.save(post);
    }

    public List<Post> getPostsByGroup(Long groupId) {
        return postRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
    }

    public List<Post> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = getPostById(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only the author can delete this post");
        }
        postRepository.delete(post);
    }

    public List<Post> searchPosts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return postRepository.findAllByOrderByCreatedAtDesc();
        }
        return postRepository.searchPosts(query);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Post> getPostsByTitle(String title) {
        return postRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title);
    }
}

