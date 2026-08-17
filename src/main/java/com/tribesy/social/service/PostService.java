package com.tribesy.social.service;

import com.tribesy.social.dto.PageResponse;
import com.tribesy.social.dto.PostDto.*;
import com.tribesy.social.entity.Post;
import com.tribesy.social.entity.User;
import com.tribesy.social.repository.FollowRepository;
import com.tribesy.social.repository.PostRepository;
import com.tribesy.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Transactional
    public PostResponse createPost(CreatePostRequest request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Post post = Post.builder()
                .content(request.content())
                .author(author)
                .build();

        Post savedPost = postRepository.save(post);

        return mapToPostResponse(savedPost);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<PostResponse> posts = postsPage.getContent().stream()
                .map(this::mapToPostResponse)
                .toList();

        return new PageResponse<>(
                posts,
                postsPage.getNumber(),
                postsPage.getSize(),
                postsPage.getTotalElements(),
                postsPage.getTotalPages(),
                postsPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getFeed(int page, int size, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<User> followingList = followRepository.findFollowingByUser(currentUser);
        List<Long> followingIds = followingList.stream().map(User::getId).toList();

        Pageable pageable = PageRequest.of(page, size);

        if (followingIds.isEmpty()) {
            return new PageResponse<>(
                    List.of(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0, true
            );
        }

        Page<Post> postsPage = postRepository.findFeedByAuthorIds(followingIds, pageable);

        List<PostResponse> posts = postsPage.getContent().stream()
                .map(this::mapToPostResponse)
                .toList();

        return new PageResponse<>(
                posts,
                postsPage.getNumber(),
                postsPage.getSize(),
                postsPage.getTotalElements(),
                postsPage.getTotalPages(),
                postsPage.isLast()
        );
    }

    private PostResponse mapToPostResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getAuthor().getUsername(),
                post.getCreatedAt()
        );
    }
}