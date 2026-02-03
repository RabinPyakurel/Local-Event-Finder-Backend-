package com.rabin.backend.repository;

import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Public event listing
    List<Event> findByEventStatus(EventStatus status);

    // Organizer: view own events
    List<Event> findByCreatedBy_Id(Long organizerId);

    // Ownership check (secure update/delete)
    Optional<Event> findByIdAndCreatedBy_Id(Long eventId, Long organizerId);
    long countByEventStatus(EventStatus status);

    boolean existsByIdAndCreatedBy_Id(Long eventId, Long userId);

    // Count events by organizer (for public profile stats)
    long countByCreatedBy_Id(Long organizerId);

    // Get organizer's events filtered by status (for public profile)
    List<Event> findByCreatedBy_IdAndEventStatus(Long organizerId, EventStatus status);
}
