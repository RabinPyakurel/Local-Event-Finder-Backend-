package com.rabin.backend.repository;

import com.rabin.backend.model.EventTag;
import com.rabin.backend.model.Group;
import com.rabin.backend.model.GroupTagMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupTagMapRepository extends JpaRepository<GroupTagMap, Long> {

    // Find all tags for a group
    List<GroupTagMap> findByGroup(Group group);

    // Find all groups with a specific tag
    List<GroupTagMap> findByTag(EventTag tag);

    // Delete all tags for a group
    void deleteByGroup(Group group);

    // Check if group has tag
    boolean existsByGroupAndTag(Group group, EventTag tag);
}
