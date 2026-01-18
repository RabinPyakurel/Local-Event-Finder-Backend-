package com.rabin.backend.repository;

import com.rabin.backend.model.EventTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventTagRepository extends JpaRepository<EventTag, Long> {
    Optional<EventTag> findByTagKey(String tagKey);
}
