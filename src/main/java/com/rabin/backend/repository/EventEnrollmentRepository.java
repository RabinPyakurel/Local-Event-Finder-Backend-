package com.rabin.backend.repository;

import com.rabin.backend.model.EventEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventEnrollmentRepository extends JpaRepository<EventEnrollment, Long> {

    // Prevent duplicate enrollment
    boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);
    Optional<EventEnrollment> findByTicketCode(String ticketCode);


    // Organizer views enrollments
    List<EventEnrollment> findByEvent_Id(Long eventId);

    // Count enrollments for an event
    long countByEvent_Id(Long eventId);

    // User views own enrollments
    List<EventEnrollment> findByUser_Id(Long userId);

    Optional<EventEnrollment> findByUser_IdAndEvent_Id(Long userId, Long eventId);
}
