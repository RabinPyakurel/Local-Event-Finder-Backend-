package com.rabin.backend.repository;

import com.rabin.backend.enums.RoleName;
import com.rabin.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
    boolean existsByName(RoleName roleName);
}
