package com.rabin.backend.repository;

import com.rabin.backend.model.User;
import com.rabin.backend.model.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUser(User user);
    void deleteByUser(User user);
}
