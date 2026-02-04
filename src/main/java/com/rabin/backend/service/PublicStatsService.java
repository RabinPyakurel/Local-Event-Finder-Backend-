package com.rabin.backend.service;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.EventTagRepository;
import com.rabin.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PublicStatsService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventTagRepository eventTagRepository;

    public PublicStatsService(UserRepository userRepository, EventRepository eventRepository, EventTagRepository eventTagRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventTagRepository = eventTagRepository;
    }

    public GenericApiResponse<Map<String, Object>> getPublicStats() {
        log.debug("Admin: Getting dashboard statistics");

        Map<String, Object> stats = new HashMap<>();

        // User statistics
        long totalUsers = userRepository.count();
        stats.put("userCount", totalUsers);


        long activeEvents = eventRepository.countByEventStatus(EventStatus.ACTIVE);
        stats.put("eventCount", activeEvents);

        long categories = eventTagRepository.count();
        stats.put("categoriesCount",categories);
        log.info("Public stats retrieved successfully");
        return GenericApiResponse.ok(200, "Public statistics retrieved successfully", stats);
    }

}
