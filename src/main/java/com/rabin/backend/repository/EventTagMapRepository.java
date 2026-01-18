package com.rabin.backend.repository;

import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventTagMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventTagMapRepository extends JpaRepository<EventTagMap, Long> {
    List<EventTagMap> findByEvent_Id(Long eventId);
    List<EventTagMap> findByEvent(Event event);
    void deleteByEvent(Event event);
}
