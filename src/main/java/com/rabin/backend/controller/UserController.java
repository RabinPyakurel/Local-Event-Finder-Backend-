package com.rabin.backend.controller;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.UpdateInterestsDto;
import com.rabin.backend.dto.request.UpdateProfileDto;
import com.rabin.backend.dto.response.UserProfileResponseDto;
import com.rabin.backend.service.UserService;
import com.rabin.backend.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<GenericApiResponse<UserProfileResponseDto>> getProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get profile request for user: {}", userId);
        GenericApiResponse<UserProfileResponseDto> response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/profile", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericApiResponse<UserProfileResponseDto>> updateProfile(
            @ModelAttribute UpdateProfileDto dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Update profile request for user: {}", userId);
        GenericApiResponse<UserProfileResponseDto> response = userService.updateProfile(userId, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/interests")
    public ResponseEntity<GenericApiResponse<List<String>>> getInterests() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Get interests request for user: {}", userId);
        GenericApiResponse<List<String>> response = userService.getUserInterests(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/interests")
    public ResponseEntity<GenericApiResponse<List<String>>> updateInterests(
            @RequestBody UpdateInterestsDto dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.debug("Update interests request for user: {}", userId);
        GenericApiResponse<List<String>> response = userService.updateUserInterests(userId, dto);
        return ResponseEntity.ok(response);
    }
}
