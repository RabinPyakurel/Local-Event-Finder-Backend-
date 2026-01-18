package com.rabin.backend.service;

import com.rabin.backend.dto.GenericApiResponse;
import com.rabin.backend.dto.response.AdminUserResponseDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.dto.response.ReportResponseDto;
import com.rabin.backend.enums.EventStatus;
import com.rabin.backend.enums.ReportStatus;
import com.rabin.backend.enums.UserStatus;
import com.rabin.backend.exception.UserNotFoundException;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventTagMap;
import com.rabin.backend.model.Report;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.EventTagMapRepository;
import com.rabin.backend.repository.ReportRepository;
import com.rabin.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReportRepository reportRepository;
    private final EventTagMapRepository eventTagMapRepository;

    public AdminService(UserRepository userRepository,
                        EventRepository eventRepository,
                        ReportRepository reportRepository,
                        EventTagMapRepository eventTagMapRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reportRepository = reportRepository;
        this.eventTagMapRepository = eventTagMapRepository;
    }

    // ==================== USER MANAGEMENT ====================

    public GenericApiResponse<List<AdminUserResponseDto>> getAllUsers(Integer page, Integer size) {
        log.debug("Admin: Getting all users, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> usersPage = userRepository.findAll(pageable);
        List<AdminUserResponseDto> users = usersPage.getContent().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} users", users.size());
        return GenericApiResponse.ok(200, "Users retrieved successfully", users);
    }

    @Transactional
    public GenericApiResponse<AdminUserResponseDto> suspendUser(Long userId) {
        log.debug("Admin: Suspending user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getUserStatus() == UserStatus.SUSPENDED) {
            return GenericApiResponse.error(400,"User is already suspended");
        }

        user.setUserStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        log.info("Admin: User {} suspended successfully", userId);
        return GenericApiResponse.ok(200, "User suspended successfully",
                mapToAdminUserResponse(user));
    }

    @Transactional
    public GenericApiResponse<AdminUserResponseDto> activateUser(Long userId) {
        log.debug("Admin: Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getUserStatus() == UserStatus.ACTIVE) {
            return GenericApiResponse.error(400,"User is already active");
        }

        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Admin: User {} activated successfully", userId);
        return GenericApiResponse.ok(200, "User activated successfully",
                mapToAdminUserResponse(user));
    }

    public GenericApiResponse<AdminUserResponseDto> getUserById(Long userId) {
        log.debug("Admin: Getting user details for: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return GenericApiResponse.ok(200, "User retrieved successfully",
                mapToAdminUserResponse(user));
    }

    // ==================== EVENT MODERATION ====================

    public GenericApiResponse<List<EventResponseDto>> getAllEvents(Integer page, Integer size) {
        log.debug("Admin: Getting all events, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Event> eventsPage = eventRepository.findAll(pageable);
        List<EventResponseDto> events = eventsPage.getContent().stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} events", events.size());
        return GenericApiResponse.ok(200, "Events retrieved successfully", events);
    }

    @Transactional
    public GenericApiResponse<Void> removeEvent(Long eventId) {
        log.debug("Admin: Removing event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        event.setEventStatus(EventStatus.INACTIVE);
        eventRepository.save(event);

        log.info("Admin: Event {} removed successfully", eventId);
        return GenericApiResponse.ok(200, "Event removed successfully", null);
    }

    @Transactional
    public GenericApiResponse<Void> deleteEvent(Long eventId) {
        log.debug("Admin: Permanently deleting event: {}", eventId);

        if (!eventRepository.existsById(eventId)) {
            return GenericApiResponse.error(404,"Event not found");
        }

        eventRepository.deleteById(eventId);

        log.info("Admin: Event {} deleted permanently", eventId);
        return GenericApiResponse.ok(200, "Event deleted permanently", null);
    }

    // ==================== REPORT HANDLING ====================

    public GenericApiResponse<List<ReportResponseDto>> getAllReports() {
        log.debug("Admin: Getting all reports");

        List<Report> reports = reportRepository.findAll();
        List<ReportResponseDto> reportDtos = reports.stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} reports", reportDtos.size());
        return GenericApiResponse.ok(200, "Reports retrieved successfully", reportDtos);
    }

    public GenericApiResponse<List<ReportResponseDto>> getPendingReports() {
        log.debug("Admin: Getting pending reports");

        List<Report> reports = reportRepository.findByReportStatus(ReportStatus.PENDING);
        List<ReportResponseDto> reportDtos = reports.stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());

        log.info("Admin: Retrieved {} pending reports", reportDtos.size());
        return GenericApiResponse.ok(200, "Pending reports retrieved successfully", reportDtos);
    }

    @Transactional
    public GenericApiResponse<ReportResponseDto> resolveReport(Long reportId) {
        log.debug("Admin: Resolving report: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setReportStatus(ReportStatus.RESOLVED);
        reportRepository.save(report);

        log.info("Admin: Report {} resolved successfully", reportId);
        return GenericApiResponse.ok(200, "Report resolved successfully",
                mapToReportResponse(report));
    }

    @Transactional
    public GenericApiResponse<ReportResponseDto> rejectReport(Long reportId) {
        log.debug("Admin: Rejecting report: {}", reportId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setReportStatus(ReportStatus.REJECTED);
        reportRepository.save(report);

        log.info("Admin: Report {} rejected successfully", reportId);
        return GenericApiResponse.ok(200, "Report rejected successfully",
                mapToReportResponse(report));
    }

    // ==================== HELPER METHODS ====================

    private AdminUserResponseDto mapToAdminUserResponse(User user) {
        AdminUserResponseDto dto = new AdminUserResponseDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setDob(user.getDob());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setUserStatus(user.getUserStatus().name());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        dto.setRoles(roles);

        return dto;
    }

    private EventResponseDto mapToEventResponse(Event event) {
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

        List<EventTagMap> tagMaps = eventTagMapRepository.findByEvent(event);
        List<String> tags = tagMaps.stream()
                .map(tm -> tm.getEventTag().getTagKey())
                .collect(Collectors.toList());
        dto.setTags(tags);

        return dto;
    }

    private ReportResponseDto mapToReportResponse(Report report) {
        ReportResponseDto dto = new ReportResponseDto();
        dto.setId(report.getId());
        dto.setReporterId(report.getReporter().getId());
        dto.setReporterName(report.getReporter().getFullName());
        dto.setReporterEmail(report.getReporter().getEmail());
        dto.setEventId(report.getEvent().getId());
        dto.setEventTitle(report.getEvent().getTitle());
        dto.setReason(report.getReason());
        dto.setReportStatus(report.getReportStatus().name());
        dto.setCreatedAt(report.getCreatedAt());

        return dto;
    }
}
