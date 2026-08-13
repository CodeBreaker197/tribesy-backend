package com.tribesy.social.controller;

import com.tribesy.social.dto.PageResponse;
import com.tribesy.social.dto.PostDto.*;
import com.tribesy.social.entity.Post;
import com.tribesy.social.entity.User;
import com.tribesy.social.repository.PostRepository;
import com.tribesy.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User author = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        Post post = Post.builder()
                .content(request.content())
                .author(author)
                .build();

        Post savedPost = postRepository.save(post);

        return ResponseEntity.ok(new PostResponse(
                savedPost.getId(),
                savedPost.getContent(),
                savedPost.getAuthor().getUsername(),
                savedPost.getCreatedAt()
        ));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<PostResponse> posts = postsPage.getContent().stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getContent(),
                        post.getAuthor().getUsername(),
                        post.getCreatedAt()
                ))
                .toList();

        PageResponse<PostResponse> response = new PageResponse<>(
                posts,
                postsPage.getNumber(),
                postsPage.getSize(),
                postsPage.getTotalElements(),
                postsPage.getTotalPages(),
                postsPage.isLast()
        );

        return ResponseEntity.ok(response);
    }
}