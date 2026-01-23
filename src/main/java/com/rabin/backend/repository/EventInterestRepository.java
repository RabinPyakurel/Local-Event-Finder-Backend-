package com.rabin.backend.repository;

import com.rabin.backend.model.EventInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventInterestRepository extends JpaRepository<EventInterest, Long> {

    // Check if user is interested in an event
    boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);

    // Find specific interest record
    Optional<EventInterest> findByUser_IdAndEvent_Id(Long userId, Long eventId);

    // Get all events user is interested in
    List<EventInterest> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // Get all users interested in an event
    List<EventInterest> findByEvent_Id(Long eventId);

    // Count interests for an event
    long countByEvent_Id(Long eventId);

    // Count user's total interested events
    long countByUser_Id(Long userId);

    // Delete by user and event
    void deleteByUser_IdAndEvent_Id(Long userId, Long eventId);
}
