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
import com.rabin.backend.util.Haversine;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        validateBasicFields(dto);

        // Validate and normalize start/end dates
        validateAndNormalizeEventDates(dto);

        // Validate organizer
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> {
                    log.error("❌ Organizer not found with ID: {}", organizerId);
                    return new IllegalArgumentException("Organizer not found");
                });

        // Validate event tags
        validateEventTags(dto.getTags());

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

        // Create and save event
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setVenue(dto.getVenue());
        event.setEventImageUrl(eventImageUrl);
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setEventStatus(EventStatus.ACTIVE);
        event.setCreatedBy(organizer);
        event.setIsPaid(dto.getIsPaid() != null ? dto.getIsPaid() : false);
        event.setPrice(dto.getPrice() != null ? dto.getPrice() : 0.0);
        event.setAvailableSeats(dto.getAvailableSeats());
        event.setBookedSeats(0);

        Event saved = eventRepository.save(event);
        log.info("Event created with id: {} by organizer: {}", saved.getId(), organizerId);

        // Save tags
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            saveEventTags(saved, dto.getTags());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public EventResponseDto updateEvent(Long eventId, CreateEventDto dto, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check organizer
        if (!event.getCreatedBy().getId().equals(organizerId)) {
            throw new IllegalArgumentException("You are not authorized to update this event");
        }

        // Update basic fields
        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) event.setTitle(dto.getTitle());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getVenue() != null) event.setVenue(dto.getVenue());
        if (dto.getLatitude() != null) event.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) event.setLongitude(dto.getLongitude());

        // Update dates if provided
        if (dto.getStartDate() != null || dto.getEndDate() != null) {
            LocalDateTime start = dto.getStartDate() != null ? dto.getStartDate() : event.getStartDate();
            LocalDateTime end = dto.getEndDate() != null ? dto.getEndDate() : event.getEndDate();

            CreateEventDto tempDto = new CreateEventDto();
            tempDto.setStartDate(start);
            tempDto.setEndDate(end);

            validateAndNormalizeEventDates(tempDto);

            event.setStartDate(tempDto.getStartDate());
            event.setEndDate(tempDto.getEndDate());
        }

        // Update paid event fields
        if (dto.getIsPaid() != null) event.setIsPaid(dto.getIsPaid());
        if (dto.getPrice() != null) event.setPrice(dto.getPrice());
        if (dto.getAvailableSeats() != null) event.setAvailableSeats(dto.getAvailableSeats());

        // Update image if provided
        if (dto.getEventImage() != null && !dto.getEventImage().isEmpty()) {
            try {
                String imageUrl = FileUtil.saveFile(dto.getEventImage(), "events");
                event.setEventImageUrl(imageUrl);
                log.debug("Event image updated: {}", imageUrl);
            } catch (Exception e) {
                log.error("Failed to save event image", e);
                throw new IllegalArgumentException("Failed to save event image: " + e.getMessage());
            }
        }

        // Update tags
        if (dto.getTags() != null) {
            eventTagMapRepository.deleteByEvent(event);
            if (!dto.getTags().isEmpty()) saveEventTags(event, dto.getTags());
        }

        Event updated = eventRepository.save(event);
        log.info("Event updated: {}", eventId);

        return mapToResponse(updated);
    }

    @Transactional
    public void cancelEvent(Long eventId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getCreatedBy().getId().equals(organizerId)) {
            throw new IllegalArgumentException("You are not authorized to cancel this event");
        }

        event.setEventStatus(EventStatus.INACTIVE);
        eventRepository.save(event);
        log.info("Event cancelled: {}", eventId);
    }

    // Get all active events (PUBLIC)
    public List<EventResponseDto> getActiveEvents() {
        return eventRepository.findByEventStatus(EventStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get event by ID (PUBLIC)
    public EventResponseDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        return mapToResponse(event);
    }

    // Get events created by a specific organizer (ORGANIZER)
    public List<EventResponseDto> getOrganizerEvents(Long organizerId) {
        // Ensure organizer exists
        userRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        return eventRepository.findByCreatedBy_Id(organizerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get events filtered by location (optional lat/lon & radius in km)
    public List<EventResponseDto> getEventsByLocation(Double lat, Double lon, Double radiusKm) {
        List<Event> events = eventRepository.findByEventStatus(EventStatus.ACTIVE);

        if (lat == null || lon == null) {
            return events.stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        double radius = radiusKm != null ? radiusKm : 50.0;

        return events.stream()
                .filter(e -> Haversine.distance(lat, lon, e.getLatitude(), e.getLongitude()) <= radius)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Search events with filters (location, radius, tags, search term)
    public List<EventResponseDto> searchEvents(Double lat, Double lon, Double radiusKm,
                                               List<String> tags, String searchTerm) {
        List<Event> events = eventRepository.findByEventStatus(EventStatus.ACTIVE);

        // Filter by location
        if (lat != null && lon != null) {
            double radius = radiusKm != null ? radiusKm : 50.0;
            events = events.stream()
                    .filter(e -> Haversine.distance(lat, lon, e.getLatitude(), e.getLongitude()) <= radius)
                    .toList();
        }

        // Filter by tags
        if (tags != null && !tags.isEmpty()) {
            events = events.stream()
                    .filter(e -> {
                        List<String> eventTags = eventTagMapRepository.findByEvent(e)
                                .stream()
                                .map(tm -> tm.getEventTag().getTagKey())
                                .toList();
                        return tags.stream().anyMatch(eventTags::contains);
                    })
                    .toList();
        }

        // Filter by search term
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchLower = searchTerm.toLowerCase();
            events = events.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(searchLower) ||
                            e.getDescription().toLowerCase().contains(searchLower) ||
                            e.getVenue().toLowerCase().contains(searchLower))
                    .toList();
        }

        return events.stream().map(this::mapToResponse).toList();
    }


    // --------------------- Helper Methods ---------------------

    private void validateBasicFields(CreateEventDto dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Event title is required");
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty())
            throw new IllegalArgumentException("Event description is required");
        if (dto.getVenue() == null || dto.getVenue().trim().isEmpty())
            throw new IllegalArgumentException("Event venue is required");

        if (dto.getLatitude() == null || dto.getLongitude() == null)
            throw new IllegalArgumentException("Latitude and Longitude are required");
        if (dto.getLatitude() < -90 || dto.getLatitude() > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        if (dto.getLongitude() < -180 || dto.getLongitude() > 180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180");

        if (dto.getIsPaid() != null && dto.getIsPaid() && (dto.getPrice() == null || dto.getPrice() <= 0))
            throw new IllegalArgumentException("Price must be greater than 0 for paid events");
        if (dto.getAvailableSeats() != null && dto.getAvailableSeats() < 0)
            throw new IllegalArgumentException("Available seats cannot be negative");
    }

    private void validateEventTags(List<String> tags) {
        if (tags == null) return;
        for (String tagName : tags) {
            try {
                InterestCategory.valueOf(tagName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid event tag: " + tagName);
            }
        }
    }

    private void validateAndNormalizeEventDates(CreateEventDto dto) {
        LocalDateTime now = LocalDateTime.now();

        if (dto.getStartDate() == null)
            throw new IllegalArgumentException("Event start date is required");
        if (dto.getStartDate().isBefore(now))
            throw new IllegalArgumentException("Event start date cannot be in the past");

        if (dto.getEndDate() == null) {
            dto.setEndDate(dto.getStartDate().plusHours(23).plusMinutes(59).plusSeconds(59));
        }

        if (dto.getEndDate().isBefore(dto.getStartDate()))
            throw new IllegalArgumentException("Event end date cannot be before start date");

        if (dto.getEndDate().isBefore(now))
            throw new IllegalArgumentException("Event end date cannot be in the past");

        // normalize seconds/nanos
        dto.setStartDate(dto.getStartDate().withSecond(0).withNano(0));
        dto.setEndDate(dto.getEndDate().withSecond(0).withNano(0));
    }

    private void saveEventTags(Event event, List<String> tagNames) {
        for (String tagName : tagNames) {
            try {
                InterestCategory category = InterestCategory.valueOf(tagName);

                EventTag tag = eventTagRepository.findByTagKey(category.name())
                        .orElseGet(() -> {
                            EventTag newTag = new EventTag();
                            newTag.setTagKey(category.name());
                            newTag.setDisplayName(category.getDisplayName());
                            return eventTagRepository.save(newTag);
                        });

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
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setOrganizerName(event.getCreatedBy().getFullName());
        dto.setEventStatus(event.getEventStatus().name());

        List<EventTagMap> tagMaps = eventTagMapRepository.findByEvent(event);
        List<String> tags = tagMaps.stream()
                .map(tm -> tm.getEventTag().getTagKey())
                .collect(Collectors.toList());
        dto.setTags(tags);

        dto.setIsPaid(event.getIsPaid());
        dto.setPrice(event.getPrice());
        dto.setAvailableSeats(event.getAvailableSeats());
        dto.setBookedSeats(event.getBookedSeats());

        return dto;
    }
}
