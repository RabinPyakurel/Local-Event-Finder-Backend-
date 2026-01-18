package com.rabin.backend.service.event;

import com.rabin.backend.dto.request.CreateEventDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.enums.InterestCategory;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventTag;
import com.rabin.backend.model.EventTagMap;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.EventTagMapRepository;
import com.rabin.backend.repository.EventTagRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.util.FileUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventTagRepository eventTagRepository;
    private final EventTagMapRepository eventTagMapRepository;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        EventTagRepository eventTagRepository,
                        EventTagMapRepository eventTagMapRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventTagRepository = eventTagRepository;
        this.eventTagMapRepository = eventTagMapRepository;
    }

    @Transactional
    public EventResponseDto createEvent(CreateEventDto dto, Long organizerId) {
        log.info("📝 Creating event: title='{}', organizerId={}", dto.getTitle(), organizerId);

        // Validate required fields
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            log.error("❌ Validation failed: Event title is required");
            throw new IllegalArgumentException("Event title is required and cannot be empty");
        }

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            log.error("❌ Validation failed: Event description is required");
            throw new IllegalArgumentException("Event description is required and cannot be empty");
        }

        if (dto.getVenue() == null || dto.getVenue().trim().isEmpty()) {
            log.error("❌ Validation failed: Event venue is required");
            throw new IllegalArgumentException("Event venue is required and cannot be empty");
        }

        if (dto.getEventDate() == null) {
            log.error("❌ Validation failed: Event date is required");
            throw new IllegalArgumentException("Event date is required");
        }

        if (dto.getLatitude() == null) {
            log.error("❌ Validation failed: Latitude is required");
            throw new IllegalArgumentException("Latitude is required");
        }

        if (dto.getLongitude() == null) {
            log.error("❌ Validation failed: Longitude is required");
            throw new IllegalArgumentException("Longitude is required");
        }

        // Validate latitude range (-90 to 90)
        if (dto.getLatitude() < -90 || dto.getLatitude() > 90) {
            log.error("❌ Validation failed: Invalid latitude value: {}", dto.getLatitude());
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }

        // Validate longitude range (-180 to 180)
        if (dto.getLongitude() < -180 || dto.getLongitude() > 180) {
            log.error("❌ Validation failed: Invalid longitude value: {}", dto.getLongitude());
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }

        // Validate paid event fields
        if (dto.getIsPaid() != null && dto.getIsPaid()) {
            if (dto.getPrice() == null || dto.getPrice() <= 0) {
                log.error("❌ Validation failed: Price must be greater than 0 for paid events");
                throw new IllegalArgumentException("Price must be greater than 0 for paid events");
            }
        }

        if (dto.getAvailableSeats() != null && dto.getAvailableSeats() < 0) {
            log.error("❌ Validation failed: Available seats cannot be negative");
            throw new IllegalArgumentException("Available seats cannot be negative");
        }

        // Validate organizer
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> {
                    log.error("❌ Organizer not found with ID: {}", organizerId);
                    return new IllegalArgumentException("Organizer not found");
                });

        // Validate event tags
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (String tagName : dto.getTags()) {
                try {
                    InterestCategory.valueOf(tagName);
                } catch (IllegalArgumentException e) {
                    log.error("❌ Invalid event tag: '{}'. Valid tags are: {}", tagName, 
                            String.join(", ", java.util.Arrays.stream(InterestCategory.values())
                                    .map(Enum::name).toArray(String[]::new)));
                    throw new IllegalArgumentException("Invalid event tag: '" + tagName + "'. Valid tags are: " + 
                            String.join(", ", java.util.Arrays.stream(InterestCategory.values())
                                    .map(Enum::name).toArray(String[]::new)));
                }
            }
        }

        // Handle event image upload
        String eventImageUrl = null;
        if (dto.getEventImage() != null && !dto.getEventImage().isEmpty()) {
            try {
                eventImageUrl = FileUtil.saveFile(dto.getEventImage(), "events");
                log.info("✅ Event image saved: {}", eventImageUrl);
            } catch (Exception e) {
                log.error("❌ Failed to save event image: {}", e.getMessage());
                throw new IllegalArgumentException("Failed to save event image: " + e.getMessage());
            }
        }

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setVenue(dto.getVenue());
        event.setEventImageUrl(eventImageUrl);
        event.setEventDate(dto.getEventDate());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setEventStatus(EventStatus.ACTIVE);
        event.setCreatedBy(organizer);

        // Set paid event fields
        event.setIsPaid(dto.getIsPaid() != null ? dto.getIsPaid() : false);
        event.setPrice(dto.getPrice() != null ? dto.getPrice() : 0.0);
        event.setAvailableSeats(dto.getAvailableSeats());
        event.setBookedSeats(0);

        Event saved = eventRepository.save(event);
        log.info("Event created with id: {} by organizer: {}", saved.getId(), organizerId);

        // Save event tags
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            saveEventTags(saved, dto.getTags());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public EventResponseDto updateEvent(Long eventId, CreateEventDto dto, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check if organizer owns this event
        if (!event.getCreatedBy().getId().equals(organizerId)) {
            throw new IllegalArgumentException("You are not authorized to update this event");
        }

        // Update fields if provided
        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getVenue() != null) {
            event.setVenue(dto.getVenue());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLatitude() != null) {
            event.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            event.setLongitude(dto.getLongitude());
        }

        // Handle event image upload
        if (dto.getEventImage() != null && !dto.getEventImage().isEmpty()) {
            try {
                String eventImageUrl = FileUtil.saveFile(dto.getEventImage(), "events");
                event.setEventImageUrl(eventImageUrl);
                log.debug("Event image updated: {}", eventImageUrl);
            } catch (Exception e) {
                log.error("Failed to save event image", e);
                throw new IllegalArgumentException("Failed to save event image: " + e.getMessage());
            }
        }

        // Update tags if provided
        if (dto.getTags() != null) {
            // Delete existing tags
            eventTagMapRepository.deleteByEvent(event);
            // Save new tags
            if (!dto.getTags().isEmpty()) {
                saveEventTags(event, dto.getTags());
            }
        }

        Event updated = eventRepository.save(event);
        log.info("Event updated: {}", eventId);

        return mapToResponse(updated);
    }

    @Transactional
    public void cancelEvent(Long eventId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check if organizer owns this event
        if (!event.getCreatedBy().getId().equals(organizerId)) {
            throw new IllegalArgumentException("You are not authorized to cancel this event");
        }

        event.setEventStatus(EventStatus.INACTIVE);
        eventRepository.save(event);
        log.info("Event cancelled: {}", eventId);
    }

    public List<EventResponseDto> getActiveEvents() {
        return eventRepository.findByEventStatus(EventStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EventResponseDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        return mapToResponse(event);
    }

    /**
     * Get events created by a specific organizer
     */
    public List<EventResponseDto> getOrganizerEvents(Long organizerId) {
        log.debug("Getting events for organizer: {}", organizerId);

        // Verify organizer exists
        userRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        return eventRepository.findByCreatedBy_Id(organizerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get events filtered by location (within radius in km)
     */
    public List<EventResponseDto> getEventsByLocation(Double lat, Double lon, Double radiusKm) {
        log.debug("Getting events near location: lat={}, lon={}, radius={}km", lat, lon, radiusKm);

        List<Event> allActiveEvents = eventRepository.findByEventStatus(EventStatus.ACTIVE);

        if (lat == null || lon == null) {
            return allActiveEvents.stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        Double radius = radiusKm != null ? radiusKm : 50.0; // Default 50km radius

        return allActiveEvents.stream()
                .filter(event -> {
                    double distance = calculateDistance(lat, lon, event.getLatitude(), event.getLongitude());
                    return distance <= radius;
                })
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Search events with multiple filters
     */
    public List<EventResponseDto> searchEvents(Double lat, Double lon, Double radiusKm,
                                                List<String> tags, String searchTerm) {
        log.debug("Searching events with filters - lat={}, lon={}, radius={}km, tags={}, searchTerm={}",
                lat, lon, radiusKm, tags, searchTerm);

        List<Event> events = eventRepository.findByEventStatus(EventStatus.ACTIVE);

        // Filter by location if provided
        if (lat != null && lon != null) {
            Double radius = radiusKm != null ? radiusKm : 50.0;
            events = events.stream()
                    .filter(event -> {
                        double distance = calculateDistance(lat, lon, event.getLatitude(), event.getLongitude());
                        return distance <= radius;
                    })
                    .toList();
        }

        // Filter by tags if provided
        if (tags != null && !tags.isEmpty()) {
            events = events.stream()
                    .filter(event -> {
                        List<String> eventTags = eventTagMapRepository.findByEvent(event)
                                .stream()
                                .map(tm -> tm.getEventTag().getTagKey())
                                .toList();
                        return tags.stream().anyMatch(eventTags::contains);
                    })
                    .toList();
        }

        // Filter by search term if provided
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchLower = searchTerm.toLowerCase();
            events = events.stream()
                    .filter(event ->
                        event.getTitle().toLowerCase().contains(searchLower) ||
                        event.getDescription().toLowerCase().contains(searchLower) ||
                        event.getVenue().toLowerCase().contains(searchLower)
                    )
                    .toList();
        }

        return events.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Helper methods

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    private void saveEventTags(Event event, List<String> tagNames) {
        for (String tagName : tagNames) {
            try {
                InterestCategory category = InterestCategory.valueOf(tagName);

                // Get or create EventTag
                EventTag tag = eventTagRepository.findByTagKey(category.name())
                        .orElseGet(() -> {
                            EventTag newTag = new EventTag();
                            newTag.setTagKey(category.name());
                            newTag.setDisplayName(category.getDisplayName());
                            return eventTagRepository.save(newTag);
                        });

                // Create EventTagMap
                EventTagMap tagMap = new EventTagMap();
                tagMap.setEvent(event);
                tagMap.setEventTag(tag);
                eventTagMapRepository.save(tagMap);

                log.debug("Saved tag {} for event {}", category, event.getId());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid tag category: {}", tagName);
            }
        }
    }

    private EventResponseDto mapToResponse(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setVenue(event.getVenue());
        dto.setEventImageUrl(event.getEventImageUrl());
        dto.setEventDate(event.getEventDate());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setOrganizerName(event.getCreatedBy().getFullName());
        dto.setEventStatus(event.getEventStatus().name());

        // Get event tags
        List<EventTagMap> tagMaps = eventTagMapRepository.findByEvent(event);
        List<String> tags = tagMaps.stream()
                .map(tm -> tm.getEventTag().getTagKey())
                .collect(Collectors.toList());
        dto.setTags(tags);

        // Set paid event fields
        dto.setIsPaid(event.getIsPaid());
        dto.setPrice(event.getPrice());
        dto.setAvailableSeats(event.getAvailableSeats());
        dto.setBookedSeats(event.getBookedSeats());

        return dto;
    }
}
