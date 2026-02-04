package com.rabin.backend.repository;

import com.rabin.backend.model.EventFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Long> {

    List<EventFeedback> findByEvent_Id(Long eventId);

    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);

    Optional<EventFeedback> findByEvent_IdAndUser_Id(Long eventId, Long userId);

    void deleteByEvent_Id(Long eventId);
}
