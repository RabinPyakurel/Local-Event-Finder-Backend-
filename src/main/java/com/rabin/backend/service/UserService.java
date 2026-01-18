package com.rabin.backend.service;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.UpdateInterestsDto;
import com.rabin.backend.dto.request.UpdateProfileDto;
import com.rabin.backend.dto.response.UserProfileResponseDto;
import com.rabin.backend.enums.InterestCategory;
import com.rabin.backend.exception.UserNotFoundException;
import com.rabin.backend.model.EventTag;
import com.rabin.backend.model.User;
import com.rabin.backend.model.UserInterest;
import com.rabin.backend.repository.EventTagRepository;
import com.rabin.backend.repository.UserInterestRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final EventTagRepository eventTagRepository;

    public UserService(UserRepository userRepository,
                       UserInterestRepository userInterestRepository,
                       EventTagRepository eventTagRepository) {
        this.userRepository = userRepository;
        this.userInterestRepository = userInterestRepository;
        this.eventTagRepository = eventTagRepository;
    }

    @Transactional(readOnly = true)
    public GenericApiResponse<UserProfileResponseDto> getUserProfile(Long userId) {
        log.debug("Getting profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserProfileResponseDto profile = buildUserProfileResponse(user);
        return GenericApiResponse.ok(200, "Profile retrieved successfully", profile);
    }

    @Transactional
    public GenericApiResponse<UserProfileResponseDto> updateProfile(Long userId, UpdateProfileDto dto) {
        log.debug("Updating profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean updated = false;

        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            user.setFullName(dto.getFullName().trim());
            updated = true;
        }

        if (dto.getDob() != null) {
            user.setDob(dto.getDob());
            updated = true;
        }

        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            try {
                String imageUrl = FileUtil.saveFile(dto.getProfileImage(), "profiles");
                user.setProfileImageUrl(imageUrl);
                updated = true;
                log.debug("Updated profile image for user: {}", userId);
            } catch (Exception e) {
                log.error("Failed to save profile image for user: {}", userId, e);
                throw new IllegalArgumentException("Failed to save profile image: " + e.getMessage());
            }
        }

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Profile updated for user: {}", userId);
        }

        UserProfileResponseDto profile = buildUserProfileResponse(user);
        return GenericApiResponse.ok(200, "Profile updated successfully", profile);
    }

    @Transactional(readOnly = true)
    public GenericApiResponse<List<String>> getUserInterests(Long userId) {
        log.debug("Getting interests for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<UserInterest> userInterests = userInterestRepository.findByUser(user);
        List<String> interests = userInterests.stream()
                .map(ui -> ui.getCategory().name())
                .collect(Collectors.toList());

        return GenericApiResponse.ok(200, "Interests retrieved successfully", interests);
    }

    @Transactional
    public GenericApiResponse<List<String>> updateUserInterests(Long userId, UpdateInterestsDto dto) {
        log.debug("Updating interests for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate all interests before updating
        if (dto.getInterests() != null) {
            for (String interest : dto.getInterests()) {
                try {
                    InterestCategory.valueOf(interest);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid interest category: " + interest);
                }
            }
        }

        // Delete existing interests
        userInterestRepository.deleteByUser(user);
        log.debug("Deleted existing interests for user: {}", userId);

        // Add new interests
        if (dto.getInterests() != null && !dto.getInterests().isEmpty()) {
            for (String interestName : dto.getInterests()) {
                InterestCategory category = InterestCategory.valueOf(interestName);

                // Get or create EventTag for this category
                EventTag tag = eventTagRepository.findByTagKey(category.name())
                        .orElseGet(() -> {
                            EventTag newTag = new EventTag();
                            newTag.setTagKey(category.name());
                            newTag.setDisplayName(category.getDisplayName());
                            return eventTagRepository.save(newTag);
                        });

                // Create UserInterest
                UserInterest userInterest = new UserInterest();
                userInterest.setUser(user);
                userInterest.setInterestTag(tag);
                userInterest.setCategory(category);
                userInterestRepository.save(userInterest);
            }
            log.info("Updated interests for user: {}", userId);
        }

        return getUserInterests(userId);
    }

    // Helper method
    private UserProfileResponseDto buildUserProfileResponse(User user) {
        UserProfileResponseDto profile = new UserProfileResponseDto();
        profile.setId(user.getId());
        profile.setFullName(user.getFullName());
        profile.setEmail(user.getEmail());
        profile.setDob(user.getDob());
        profile.setProfileImageUrl(user.getProfileImageUrl());

        // Get user interests
        List<UserInterest> userInterests = userInterestRepository.findByUser(user);
        List<String> interests = userInterests.stream()
                .map(ui -> ui.getCategory().name())
                .collect(Collectors.toList());
        profile.setInterests(interests);

        // Get user roles
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        profile.setRoles(roles);

        return profile;
    }
}
