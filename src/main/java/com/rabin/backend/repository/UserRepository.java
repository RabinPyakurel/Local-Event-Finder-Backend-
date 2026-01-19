package com.rabin.backend.repository;

import com.rabin.backend.enums.UserStatus;
import com.rabin.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByUserStatus(UserStatus status);
}
