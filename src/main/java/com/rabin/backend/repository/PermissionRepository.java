package com.rabin.backend.repository;

import com.rabin.backend.enums.PermissionName;
import com.rabin.backend.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(PermissionName name);
    boolean existsByName(PermissionName name);
}
