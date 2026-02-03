package com.rabin.backend.service.auth;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.request.AdminRegisterDto;
import com.rabin.backend.dto.request.ChangePasswordDto;
import com.rabin.backend.dto.request.LoginDto;
import com.rabin.backend.dto.request.RegisterDto;
import com.rabin.backend.dto.response.UserAuthResponseDto;
import com.rabin.backend.enums.InterestCategory;
import com.rabin.backend.enums.RoleName;
import com.rabin.backend.exception.InvalidCredentialsException;
import com.rabin.backend.exception.UserNotFoundException;
import com.rabin.backend.model.EventTag;
import com.rabin.backend.model.Role;
import com.rabin.backend.model.User;
import com.rabin.backend.model.UserInterest;
import com.rabin.backend.repository.EventTagRepository;
import com.rabin.backend.repository.RoleRepository;
import com.rabin.backend.repository.UserInterestRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.util.FileUtil;
import com.rabin.backend.util.JwtService;
import com.rabin.backend.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EventTagRepository eventTagRepository;
    private final UserInterestRepository userInterestRepository;

    @Value("${app.admin.secret-key}")
    private String adminSecretKey;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EventTagRepository eventTagRepository,
                       UserInterestRepository userInterestRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventTagRepository = eventTagRepository;
        this.userInterestRepository = userInterestRepository;
    }

    @Transactional
    public UserAuthResponseDto registerUser(RegisterDto dto) {
        log.debug("Register request received for email: {}", dto.getEmail());
        validateRegistrationInput(dto);

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registration failed - email already exists: {}", dto.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        // Use USER role as default if not provided
        // IMPORTANT: Prevent ADMIN registration through regular endpoint
        RoleName roleName = dto.getRole() != null ? dto.getRole() : RoleName.USER;
        if (roleName == RoleName.ADMIN) {
            log.warn("Attempt to register as ADMIN through regular endpoint blocked: {}", dto.getEmail());
            throw new IllegalArgumentException("Admin registration requires special authorization");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Role not found: {}", roleName);
                    return new IllegalArgumentException("Role not found: " + roleName);
                });

        // Handle profile image upload
        String profileImageUrl = null;
        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            try {
                profileImageUrl = FileUtil.saveFile(dto.getProfileImage(), "profiles");
                log.debug("Profile image saved: {}", profileImageUrl);
            } catch (Exception e) {
                log.error("Failed to save profile image", e);
                throw new IllegalArgumentException("Failed to save profile image: " + e.getMessage());
            }
        }

        User user = new User();
        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setDob(dto.getDob());
        user.setProfileImageUrl(profileImageUrl);
        user.setRoles(Collections.singleton(role));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getId());

        // Save user interests
        if (dto.getInterests() != null && !dto.getInterests().isEmpty()) {
            saveUserInterests(user, dto.getInterests());
        }

        String token = jwtService.generateToken(user);
        return buildUserAuthResponse(user, token, "Registration successful");
    }

    @Transactional
    public UserAuthResponseDto login(LoginDto dto) {
        log.debug("Login attempt for email: {}", dto.getEmail());
        validateLoginInput(dto);

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", dto.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for user: {}", user.getId());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getId());
        String token = jwtService.generateToken(user);
        return buildUserAuthResponse(user, token, "Login successful");
    }

    @Transactional
    public GenericApiResponse<Void> changePassword(ChangePasswordDto dto, Long userId) {
        log.debug("Password change attempt for user: {}", userId);

        if (dto == null || dto.getOldPassword() == null || dto.getNewPassword() == null || dto.getConfirmPassword() == null) {
            log.warn("Password change failed - missing data");
            throw new IllegalArgumentException("All password fields are required");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            log.warn("Password change failed - new password and confirm do not match");
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Password change failed - user not found: {}", userId);
                    return new UserNotFoundException("User not found");
                });

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            log.warn("Password change failed - incorrect old password for user: {}", userId);
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            log.warn("Password change failed - new password same as old for user: {}", userId);
            throw new IllegalArgumentException("New password cannot be same as old password");
        }

        String pwdError = ValidationUtil.validatePassword(dto.getNewPassword());
        if (pwdError != null) {
            throw new IllegalArgumentException(pwdError);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
        return GenericApiResponse.ok(200, "Password changed successfully", null);
    }

    // ----------------------- Admin Auth Methods -----------------------

    @Transactional
    public UserAuthResponseDto registerAdmin(AdminRegisterDto dto) {
        log.debug("Admin register request received for email: {}", dto.getEmail());

        // Validate admin secret key
        if (dto.getAdminSecretKey() == null || !dto.getAdminSecretKey().equals(adminSecretKey)) {
            log.warn("Admin registration failed - invalid secret key for: {}", dto.getEmail());
            throw new IllegalArgumentException("Invalid admin secret key");
        }

        // Validate basic fields
        if (dto.getFullName() == null || dto.getFullName().trim().length() < 2) {
            throw new IllegalArgumentException("Full name must be at least 2 characters long");
        }
        if (!ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        String pwdError = ValidationUtil.validatePassword(dto.getPassword());
        if (pwdError != null) {
            throw new IllegalArgumentException(pwdError);
        }
        if (dto.getDob() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        int age = Period.between(dto.getDob(), LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException("Admin must be at least 18 years old");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Admin registration failed - email already exists: {}", dto.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> {
                    log.error("ADMIN role not found in database");
                    return new IllegalArgumentException("Admin role not configured");
                });

        // Handle profile image upload
        String profileImageUrl = null;
        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            try {
                profileImageUrl = FileUtil.saveFile(dto.getProfileImage(), "profiles");
                log.debug("Admin profile image saved: {}", profileImageUrl);
            } catch (Exception e) {
                log.error("Failed to save admin profile image", e);
                throw new IllegalArgumentException("Failed to save profile image: " + e.getMessage());
            }
        }

        User admin = new User();
        admin.setFullName(dto.getFullName().trim());
        admin.setEmail(dto.getEmail().trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setDob(dto.getDob());
        admin.setProfileImageUrl(profileImageUrl);
        admin.setRoles(Collections.singleton(adminRole));
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        userRepository.save(admin);
        log.info("Admin registered successfully: {} (ID: {})", admin.getEmail(), admin.getId());

        String token = jwtService.generateToken(admin);
        return buildUserAuthResponse(admin, token, "Admin registration successful");
    }

    @Transactional
    public UserAuthResponseDto adminLogin(LoginDto dto) {
        log.debug("Admin login attempt for email: {}", dto.getEmail());
        validateLoginInput(dto);

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Admin login failed - user not found: {}", dto.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        // Verify user has ADMIN role
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ADMIN);

        if (!isAdmin) {
            log.warn("Admin login failed - user is not an admin: {}", dto.getEmail());
            throw new InvalidCredentialsException("Access denied. Admin privileges required.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Admin login failed - invalid password for: {}", user.getId());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Admin logged in successfully: {}", user.getId());
        String token = jwtService.generateToken(user);
        return buildUserAuthResponse(user, token, "Admin login successful");
    }

    // ----------------------- Helper Methods -----------------------

    private void validateRegistrationInput(RegisterDto dto) {
        if (dto.getFullName() == null || dto.getFullName().trim().length() < 2) {
            throw new IllegalArgumentException("Full name must be at least 2 characters long");
        }
        if (!ValidationUtil.isValidEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
//        if (dto.getPhoneNumber() != null && !ValidationUtil.isValidPhoneNumber(dto.getPhoneNumber())) {
//            throw new IllegalArgumentException("Invalid phone number");
//        }
        String pwdError = ValidationUtil.validatePassword(dto.getPassword());
        if (pwdError != null) {
            throw new IllegalArgumentException(pwdError);
        }

        // Validate DOB - must be at least 13 years old
        if (dto.getDob() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        int age = Period.between(dto.getDob(), LocalDate.now()).getYears();
        if (age < 13) {
            throw new IllegalArgumentException("You must be at least 13 years old to register");
        }
        // Validate interests if provided
        if (dto.getInterests() != null && !dto.getInterests().isEmpty()) {
            for (String interest : dto.getInterests()) {
                try {
                    InterestCategory.valueOf(interest);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid interest category: " + interest);
                }
            }
        }
    }

    private void saveUserInterests(User user, java.util.List<String> interests) {
        for (String interestName : interests) {
            try {
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

                log.debug("Saved interest {} for user {}", category, user.getId());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid interest category: {}", interestName);
            }
        }
    }

    private void validateLoginInput(LoginDto dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    private UserAuthResponseDto buildUserAuthResponse(User user, String accessToken, String message) {
        UserAuthResponseDto response = new UserAuthResponseDto();
        response.setAccessToken(accessToken);
        response.setMessage(message);
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        response.setRoles(roles);
        return response;
    }
}
