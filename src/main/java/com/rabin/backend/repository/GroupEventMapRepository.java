package com.rabin.backend.repository;

import com.rabin.backend.model.Event;
import com.rabin.backend.model.Group;
import com.rabin.backend.model.GroupEventMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupEventMapRepository extends JpaRepository<GroupEventMap, Long> {

    // Find all events in a group
    List<GroupEventMap> findByGroup(Group group);

    // Find all groups an event belongs to
    List<GroupEventMap> findByEvent(Event event);

    // Find specific mapping
    Optional<GroupEventMap> findByGroupAndEvent(Group group, Event event);

    // Check if event belongs to group
    boolean existsByGroupAndEvent(Group group, Event event);

    // Delete mapping
    void deleteByGroupAndEvent(Group group, Event event);
}
