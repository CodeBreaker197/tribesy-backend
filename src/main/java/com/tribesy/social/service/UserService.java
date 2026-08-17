package com.tribesy.social.service;

import com.tribesy.social.dto.UpdateProfileRequest;
import com.tribesy.social.dto.UserProfileResponse;
import com.tribesy.social.dto.UserSummaryDto;
import com.tribesy.social.entity.Follow;
import com.tribesy.social.entity.User;
import com.tribesy.social.repository.FollowRepository;
import com.tribesy.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public List<UserSummaryDto> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        return userRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public UserProfileResponse getCurrentUserProfile() {
        User currentUser = getAuthenticatedUser();
        return toProfileResponse(currentUser);
    }

    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User with username '" + username + "' not found"));
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User currentUser = getAuthenticatedUser();

        if (request.avatarUrl() != null) {
            currentUser.setAvatarUrl(request.avatarUrl());
        }
        if (request.bio() != null) {
            currentUser.setBio(request.bio());
        }

        User updatedUser = userRepository.save(currentUser);
        return toProfileResponse(updatedUser);
    }

    @Transactional
    public String followUser(String username) {
        User currentUser = getAuthenticatedUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("You can't follow yourself");
        }

        if (followRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            throw new IllegalArgumentException("You already subscribed to this user");
        }

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(targetUser)
                .build();

        followRepository.save(follow);
        return "You have successfully subscribed to " + username;
    }

    @Transactional
    public String unfollowUser(String username) {
        User currentUser = getAuthenticatedUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Follow follow = followRepository.findByFollowerAndFollowing(currentUser, targetUser)
                .orElseThrow(() -> new RuntimeException("You haven't subscribed to " + username));

        followRepository.delete(follow);
        return "You unsubscribed from " + username;
    }

    public List<UserSummaryDto> getFollowers(String username) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return followRepository.findFollowersByUser(targetUser)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public List<UserSummaryDto> getFollowing(String username) {
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return followRepository.findFollowingByUser(targetUser)
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public List<UserSummaryDto> getAllUsersForAdmin() {
        return userRepository.findAll()
                .stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Authorized user not found: " + currentUsername));
    }

    private UserSummaryDto toSummaryDto(User user) {
        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .build();
    }

    private UserProfileResponse toProfileResponse(User user) {
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