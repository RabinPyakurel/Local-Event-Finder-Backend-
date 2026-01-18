package com.rabin.backend.config;

import com.rabin.backend.enums.InterestCategory;
import com.rabin.backend.enums.PermissionName;
import com.rabin.backend.enums.RoleName;
import com.rabin.backend.model.EventTag;
import com.rabin.backend.model.Permission;
import com.rabin.backend.model.Role;
import com.rabin.backend.repository.EventTagRepository;
import com.rabin.backend.repository.PermissionRepository;
import com.rabin.backend.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository,
                                   PermissionRepository permissionRepository,
                                   EventTagRepository eventTagRepository) {
        return args -> {
            log.info("Starting data initialization...");

            // Initialize Permissions
            initializePermissions(permissionRepository);

            // Initialize Roles with Permissions
            initializeRoles(roleRepository, permissionRepository);

            // Initialize Event Tags from InterestCategory
            initializeEventTags(eventTagRepository);

            log.info("Data initialization completed successfully!");
        };
    }

    private void initializePermissions(PermissionRepository permissionRepository) {
        log.info("Initializing permissions...");

        for (PermissionName permName : PermissionName.values()) {
            if (!permissionRepository.existsByName(permName)) {
                Permission permission = new Permission();
                permission.setName(permName);
                permissionRepository.save(permission);
                log.debug("Created permission: {}", permName);
            }
        }

        log.info("Permissions initialized: {} total", permissionRepository.count());
    }

    private void initializeRoles(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        log.info("Initializing roles...");

        // USER Role - Basic permissions
        if (!roleRepository.existsByName(RoleName.USER)) {
            Role userRole = new Role();
            userRole.setName(RoleName.USER);

            Set<Permission> userPermissions = new HashSet<>();
            userPermissions.add(permissionRepository.findByName(PermissionName.EVENT_READ).orElseThrow());
            userPermissions.add(permissionRepository.findByName(PermissionName.EVENT_ENROLL).orElseThrow());
            userPermissions.add(permissionRepository.findByName(PermissionName.EVENT_FEEDBACK).orElseThrow());
            userPermissions.add(permissionRepository.findByName(PermissionName.EVENT_REPORT).orElseThrow());
            userPermissions.add(permissionRepository.findByName(PermissionName.USER_FOLLOW).orElseThrow());

            userRole.setPermissions(userPermissions);
            roleRepository.save(userRole);
            log.info("Created USER role with {} permissions", userPermissions.size());
        }

        // ORGANIZER Role - User permissions + event management
        if (!roleRepository.existsByName(RoleName.ORGANIZER)) {
            Role organizerRole = new Role();
            organizerRole.setName(RoleName.ORGANIZER);

            Set<Permission> organizerPermissions = new HashSet<>();
            // All user permissions
            organizerPermissions.add(permissionRepository.findByName(PermissionName.EVENT_READ).orElseThrow());
            organizerPermissions.add(permissionRepository.findByName(PermissionName.EVENT_ENROLL).orElseThrow());
            organizerPermissions.add(permissionRepository.findByName(PermissionName.EVENT_FEEDBACK).orElseThrow());
            organizerPermissions.add(permissionRepository.findByName(PermissionName.EVENT_REPORT).orElseThrow());
            organizerPermissions.add(permissionRepository.findByName(PermissionName.USER_FOLLOW).orElseThrow());
            // Plus event management
            organizerPermissions.add(permissionRepository.findByName(PermissionName.EVENT_CREATE).orElseThrow());
            organizerPermissions.add(permissionRepository.findByName(PermissionName.EVENT_UPDATE).orElseThrow());
            organizerPermissions.add(permissionRepository.findByName(PermissionName.EVENT_DELETE).orElseThrow());
            organizerPermissions.add(permissionRepository.findByName(PermissionName.GROUP_MANAGE).orElseThrow());

            organizerRole.setPermissions(organizerPermissions);
            roleRepository.save(organizerRole);
            log.info("Created ORGANIZER role with {} permissions", organizerPermissions.size());
        }

        // ADMIN Role - All permissions
        if (!roleRepository.existsByName(RoleName.ADMIN)) {
            Role adminRole = new Role();
            adminRole.setName(RoleName.ADMIN);

            Set<Permission> adminPermissions = new HashSet<>();
            for (PermissionName permName : PermissionName.values()) {
                adminPermissions.add(permissionRepository.findByName(permName).orElseThrow());
            }

            adminRole.setPermissions(adminPermissions);
            roleRepository.save(adminRole);
            log.info("Created ADMIN role with {} permissions", adminPermissions.size());
        }

        log.info("Roles initialized: {} total", roleRepository.count());
    }

    private void initializeEventTags(EventTagRepository eventTagRepository) {
        log.info("Initializing event tags from InterestCategory...");

        int createdCount = 0;
        for (InterestCategory category : InterestCategory.values()) {
            if (!eventTagRepository.findByTagKey(category.name()).isPresent()) {
                EventTag tag = new EventTag();
                tag.setTagKey(category.name());
                tag.setDisplayName(category.getDisplayName());
                eventTagRepository.save(tag);
                log.debug("Created event tag: {} ({})", category.name(), category.getDisplayName());
                createdCount++;
            }
        }

        log.info("Event tags initialized: {} created, {} total",
                createdCount, eventTagRepository.count());
    }
}