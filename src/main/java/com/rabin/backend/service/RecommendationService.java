package com.rabin.backend.service;

import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventTagMap;
import com.rabin.backend.model.User;
import com.rabin.backend.model.UserInterest;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.EventTagMapRepository;
import com.rabin.backend.repository.UserInterestRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.util.Haversine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final EventTagMapRepository eventTagMapRepository;
    private final UserFollowService userFollowService;
    private final com.rabin.backend.repository.EventEnrollmentRepository eventEnrollmentRepository;

    // Weight constants for scoring algorithm
    private static final double ALPHA = 0.3;  // Weight for content similarity
    private static final double BETA = 0.5;   // Weight for location proximity
    private static final double SOCIAL_BOOST = 0.2;  // Boost for social connections
    private static final double MAX_DISTANCE_KM = 10.0;  // Max distance for normalization

    public RecommendationService(EventRepository eventRepository,
                                  UserRepository userRepository,
                                  UserInterestRepository userInterestRepository,
                                  EventTagMapRepository eventTagMapRepository,
                                  UserFollowService userFollowService,
                                  com.rabin.backend.repository.EventEnrollmentRepository eventEnrollmentRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.userInterestRepository = userInterestRepository;
        this.eventTagMapRepository = eventTagMapRepository;
        this.userFollowService = userFollowService;
        this.eventEnrollmentRepository = eventEnrollmentRepository;
    }

    /**
     * Get personalized event recommendations for a user
     * @param userId User ID
     * @param userLat User's current latitude
     * @param userLon User's current longitude
     * @param limit Maximum number of recommendations
     * @return List of recommended events sorted by relevance score
     */
    public List<EventResponseDto> getRecommendations(Long userId, Double userLat, Double userLon, Integer limit) {
        log.debug("Getting recommendations for user: {}, lat: {}, lon: {}, limit: {}",
                  userId, userLat, userLon, limit);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Get user interests as tag keys
        List<UserInterest> userInterests = userInterestRepository.findByUser(user);
        Set<String> userTagKeys = userInterests.stream()
                .map(ui -> ui.getInterestTag().getTagKey())
                .collect(Collectors.toSet());

        log.debug("User has {} interests: {}", userTagKeys.size(), userTagKeys);

        // If user has no interests, return events sorted by distance only
        if (userTagKeys.isEmpty()) {
            log.info("User {} has no interests, returning events sorted by distance", userId);
            return getEventsByDistance(userLat, userLon, limit);
        }

        // Get user's followed users for social boosting
        List<Long> followedUserIds = userFollowService.getFollowedUserIds(userId);
        log.debug("User follows {} users", followedUserIds.size());

        // Get all active events
        List<Event> activeEvents = eventRepository.findByEventStatus(EventStatus.ACTIVE);
        log.debug("Found {} active events", activeEvents.size());

        // Calculate scores for each event
        List<EventWithScore> scoredEvents = activeEvents.stream()
                .map(event -> {
                    // Get event tags
                    List<EventTagMap> eventTagMaps = eventTagMapRepository.findByEvent(event);
                    Set<String> eventTagKeys = eventTagMaps.stream()
                            .map(etm -> etm.getEventTag().getTagKey())
                            .collect(Collectors.toSet());

                    // Calculate content score (Jaccard similarity)
                    double contentScore = calculateJaccardSimilarity(userTagKeys, eventTagKeys);

                    // Calculate location score
                    double locationScore = 0.0;
                    if (userLat != null && userLon != null &&
                        event.getLatitude() != null && event.getLongitude() != null) {
                        double distance = Haversine.distance(userLat, userLon,
                                                             event.getLatitude(), event.getLongitude());
                        locationScore = calculateLocationScore(distance);
                    }

                    // Calculate social boost
                    double socialBoost = calculateSocialBoost(event, followedUserIds);

                    // Calculate final weighted score with social boost
                    double finalScore = (ALPHA * contentScore) + (BETA * locationScore) + socialBoost;

                    log.debug("Event {}: content={}, location={}, social={}, final={}",
                              event.getTitle(), contentScore, locationScore, socialBoost, finalScore);

                    return new EventWithScore(event, finalScore);
                })
                .filter(ews -> ews.score > 0.0)  // Only include events with positive scores
                .sorted((e1, e2) -> Double.compare(e2.score, e1.score))  // Sort descending by score
                .limit(limit != null && limit > 0 ? limit : 10)
                .toList();

        log.info("Returning {} recommended events for user {}", scoredEvents.size(), userId);

        // Convert to DTOs
        return scoredEvents.stream()
                .map(ews -> mapToResponseWithScore(ews.event, ews.score))
                .collect(Collectors.toList());
    }

    /**
     * Get events sorted by distance (for users with no interests)
     */
    private List<EventResponseDto> getEventsByDistance(Double userLat, Double userLon, Integer limit) {
        List<Event> activeEvents = eventRepository.findByEventStatus(EventStatus.ACTIVE);

        if (userLat == null || userLon == null) {
            // No location provided, just return recent events
            return activeEvents.stream()
                    .sorted((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()))
                    .limit(limit != null && limit > 0 ? limit : 10)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return activeEvents.stream()
                .filter(e -> e.getLatitude() != null && e.getLongitude() != null)
                .map(event -> {
                    double distance = Haversine.distance(userLat, userLon,
                                                         event.getLatitude(), event.getLongitude());
                    return new EventWithScore(event, 1.0 / (1.0 + distance));  // Closer = higher score
                })
                .sorted((e1, e2) -> Double.compare(e2.score, e1.score))
                .limit(limit != null && limit > 0 ? limit : 10)
                .map(ews -> mapToResponseWithScore(ews.event, ews.score))
                .collect(Collectors.toList());
    }

    /**
     * Calculate Jaccard similarity between two sets
     * Jaccard = |A ∩ B| / |A ∪ B|
     */
    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 0.0;
        }

        // Calculate intersection
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // Calculate union
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * Calculate location score based on distance
     * Closer events get higher scores
     */
    private double calculateLocationScore(double distanceKm) {
        if (distanceKm <= 0) {
            return 1.0;  // Same location
        }

        // Normalize distance: 1.0 at distance 0, approaching 0 as distance increases
        // Use exponential decay for better scoring
        return Math.max(0.0, 1.0 - (distanceKm / MAX_DISTANCE_KM));
    }

    /**
     * Calculate social boost for event based on connections
     * Boost events created by or attended by followed users
     */
    private double calculateSocialBoost(Event event, List<Long> followedUserIds) {
        if (followedUserIds.isEmpty()) {
            return 0.0;
        }

        // Check if event creator is followed
        boolean creatorFollowed = followedUserIds.contains(event.getCreatedBy().getId());

        // Check if any followed users are attending
        long attendingFollowedUsers = eventEnrollmentRepository.findByEvent_Id(event.getId())
                .stream()
                .filter(enrollment -> followedUserIds.contains(enrollment.getUser().getId()))
                .count();

        // Apply boost
        double boost = 0.0;
        if (creatorFollowed) {
            boost += SOCIAL_BOOST * 0.6;  // 60% of social boost for creator
        }
        if (attendingFollowedUsers > 0) {
            // Boost based on number of followed users attending (diminishing returns)
            boost += SOCIAL_BOOST * 0.4 * Math.min(1.0, attendingFollowedUsers / 3.0);
        }

        return boost;
    }

    // Helper methods

    private EventResponseDto mapToResponse(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setVenue(event.getVenue());
        dto.setEventImageUrl(event.getEventImageUrl());
        dto.setStartDate(event.getStartDate());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setOrganizerName(event.getCreatedBy().getFullName());
        dto.setEventStatus(event.getEventStatus().name());

        List<EventTagMap> tagMaps = eventTagMapRepository.findByEvent(event);
        List<String> tags = tagMaps.stream()
                .map(tm -> tm.getEventTag().getTagKey())
                .collect(Collectors.toList());
        dto.setTags(tags);

        return dto;
    }

    private EventResponseDto mapToResponseWithScore(Event event, double score) {
        EventResponseDto dto = mapToResponse(event);
        dto.setFinalScore(score);
        return dto;
    }

    // Inner class to hold event with its calculated score
    private static class EventWithScore {
        final Event event;
        final double score;

        EventWithScore(Event event, double score) {
            this.event = event;
            this.score = score;
        }
    }
}
