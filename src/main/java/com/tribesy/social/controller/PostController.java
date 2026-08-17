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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            Principal principal
    ) {
        String currentUsername = principal.getName();

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

    @GetMapping("/feed")
    public ResponseEntity<PageResponse<PostResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        String currentUsername = principal.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        List<Long> followingIds = currentUser.getFollowing().stream()
                .map(User::getId)
                .toList();

        Pageable pageable = PageRequest.of(page, size);

        if (followingIds.isEmpty()) {
            return ResponseEntity.ok(new PageResponse<>(
                    List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0, true
            ));
        }

        Page<Post> postsPage = postRepository.findFeedByAuthorIds(followingIds, pageable);

        List<PostResponse> posts = postsPage.getContent().stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getContent(),
                        post.getAuthor().getUsername(),
                        post.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(new PageResponse<>(
                posts,
                postsPage.getNumber(),
                postsPage.getSize(),
                postsPage.getTotalElements(),
                postsPage.getTotalPages(),
                postsPage.isLast()
        ));
    }
}