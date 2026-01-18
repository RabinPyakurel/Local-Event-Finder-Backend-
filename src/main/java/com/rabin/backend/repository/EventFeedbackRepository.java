package com.rabin.backend.repository;

import com.rabin.backend.model.EventFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Long> {

    List<EventFeedback> findByEvent_Id(Long eventId);

    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);
}
