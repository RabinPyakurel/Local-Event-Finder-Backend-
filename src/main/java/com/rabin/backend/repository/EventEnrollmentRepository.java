package com.rabin.backend.repository;

import com.rabin.backend.model.EventEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventEnrollmentRepository extends JpaRepository<EventEnrollment, Long> {

    // Check if user has any enrollment for event
    boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);

    // Find by ticket code
    Optional<EventEnrollment> findByTicketCode(String ticketCode);

    // Organizer views enrollments
    List<EventEnrollment> findByEvent_Id(Long eventId);

    // Count enrollments for an event
    long countByEvent_Id(Long eventId);

    // Count user's tickets for a specific event
    long countByUser_IdAndEvent_Id(Long userId, Long eventId);

    // User views own enrollments
    List<EventEnrollment> findByUser_Id(Long userId);

    // Get all user's tickets for an event
    List<EventEnrollment> findByUser_IdAndEvent_Id(Long userId, Long eventId);

    // Get first/any user ticket for event (for backward compatibility)
    Optional<EventEnrollment> findFirstByUser_IdAndEvent_Id(Long userId, Long eventId);

    // Delete all enrollments for an event
    void deleteByEvent_Id(Long eventId);
}
