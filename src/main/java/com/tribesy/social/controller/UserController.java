package com.tribesy.social.controller;

import com.tribesy.social.dto.UpdateProfileRequest;
import com.tribesy.social.dto.UserProfileResponse;
import com.tribesy.social.dto.UserSummaryDto;
import com.tribesy.social.entity.Follow;
import com.tribesy.social.entity.User;
import com.tribesy.social.repository.FollowRepository;
import com.tribesy.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        User currentUser = getAuthenticatedUser();
        return ResponseEntity.ok(toResponse(currentUser));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User with username '" + username + "' not found"));

        return ResponseEntity.ok(toResponse(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(@RequestBody UpdateProfileRequest request) {
        User currentUser = getAuthenticatedUser();

        if (request.avatarUrl() != null) {
            currentUser.setAvatarUrl(request.avatarUrl());
        }
        if (request.bio() != null) {
            currentUser.setBio(request.bio());
        }

        User updatedUser = userRepository.save(currentUser);
        return ResponseEntity.ok(toResponse(updatedUser));
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<String> followUser(@PathVariable String username) {
        User currentUser = getAuthenticatedUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getId().equals(targetUser.getId())) {
            return ResponseEntity.badRequest().body("You can't follow yourself");
        }

        if (followRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            return ResponseEntity.badRequest().body("You already subscribed to this user");
        }

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(targetUser)
                .build();

        followRepository.save(follow);
        return ResponseEntity.ok("You have successfully subscribed to " + username);
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<String> unfollowUser(@PathVariable String username) {
        User currentUser = getAuthenticatedUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Follow follow = followRepository.findByFollowerAndFollowing(currentUser, targetUser)
                .orElseThrow(() -> new RuntimeException("You haven't subscribed to " + username));

        followRepository.delete(follow);
        return ResponseEntity.ok("You unsubscribed from " + username);
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<List<UserSummaryDto>> getFollowers(@PathVariable String username) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserSummaryDto> followers = followRepository.findFollowersByUser(targetUser)
                .stream()
                .map(user -> UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .avatarUrl(user.getAvatarUrl())
                        .bio(user.getBio())
                        .build())
                .toList();

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<List<UserSummaryDto>> getFollowing(@PathVariable String username) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserSummaryDto> following = followRepository.findFollowingByUser(targetUser)
                .stream()
                .map(user -> UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .avatarUrl(user.getAvatarUrl())
                        .bio(user.getBio())
                        .build())
                .toList();

        return ResponseEntity.ok(following);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<UserSummaryDto>> getAllUsersForAdmin() {
        List<UserSummaryDto> users = userRepository.findAll()
                .stream()
                .map(user -> UserSummaryDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .avatarUrl(user.getAvatarUrl())
                        .bio(user.getBio())
                        .build())
                .toList();

        return ResponseEntity.ok(users);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Authorized user not found: " + currentUsername));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getCreatedAt()
        );
    }
}