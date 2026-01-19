package com.rabin.backend.service;

import com.rabin.backend.dto.request.GroupRequestDto;
import com.rabin.backend.dto.response.EventResponseDto;
import com.rabin.backend.dto.response.GroupMembershipResponseDto;
import com.rabin.backend.dto.response.GroupResponseDto;
import com.rabin.backend.enums.MembershipStatus;
import com.rabin.backend.model.Event;
import com.rabin.backend.model.EventTag;
import com.rabin.backend.model.Group;
import com.rabin.backend.model.GroupEventMap;
import com.rabin.backend.model.GroupMembership;
import com.rabin.backend.model.GroupTagMap;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.EventRepository;
import com.rabin.backend.repository.EventTagRepository;
import com.rabin.backend.repository.GroupEventMapRepository;
import com.rabin.backend.repository.GroupMembershipRepository;
import com.rabin.backend.repository.GroupRepository;
import com.rabin.backend.repository.GroupTagMapRepository;
import com.rabin.backend.repository.UserRepository;
import com.rabin.backend.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing groups and group memberships
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final GroupEventMapRepository eventMapRepository;
    private final GroupTagMapRepository tagMapRepository;
    private final UserRepository userRepository;
    private final EventTagRepository eventTagRepository;
    private final EventRepository eventRepository;

    /**
     * Create a new group
     * Only ORGANIZER or ADMIN can create groups
     */
    @Transactional
    public GroupResponseDto createGroup(Long creatorId, GroupRequestDto dto) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        Group group = new Group();
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setCreatedBy(creator);
        group.setRequiresApproval(dto.getRequiresApproval() != null ? dto.getRequiresApproval() : false);

        // Handle group image upload
        if (dto.getGroupImage() != null && !dto.getGroupImage().isEmpty()) {
            try {
                String imageUrl = FileUtil.saveFile(dto.getGroupImage(), "uploads/groups/");
                group.setGroupImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save group image", e);
            }
        }

        group = groupRepository.save(group);
        log.info("Group created: {} by user {}", group.getName(), creatorId);

        // Map tags to group
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            for (String tagName : dto.getTags()) {
                EventTag tag = eventTagRepository.findByTagKey(tagName)
                        .orElse(null);
                if (tag != null) {
                    GroupTagMap tagMap = new GroupTagMap();
                    tagMap.setGroup(group);
                    tagMap.setTag(tag);
                    tagMapRepository.save(tagMap);
                }
            }
        }

        // Auto-join creator as admin member
        GroupMembership creatorMembership = new GroupMembership();
        creatorMembership.setUser(creator);
        creatorMembership.setGroup(group);
        creatorMembership.setStatus(MembershipStatus.ACTIVE);
        creatorMembership.setIsAdmin(true);
        membershipRepository.save(creatorMembership);

        return mapToGroupResponseDto(group, creator);
    }

    /**
     * Update group
     */
    @Transactional
    public GroupResponseDto updateGroup(Long groupId, Long userId, GroupRequestDto dto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is creator
        if (!group.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Only group creator can update the group");
        }

        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setRequiresApproval(dto.getRequiresApproval() != null ? dto.getRequiresApproval() : false);

        // Handle group image upload
        if (dto.getGroupImage() != null && !dto.getGroupImage().isEmpty()) {
            try {
                String imageUrl = FileUtil.saveFile(dto.getGroupImage(), "uploads/groups/");
                group.setGroupImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save group image", e);
            }
        }

        group = groupRepository.save(group);

        // Update tags
        if (dto.getTags() != null) {
            tagMapRepository.deleteByGroup(group);
            for (String tagName : dto.getTags()) {
                EventTag tag = eventTagRepository.findByTagKey(tagName).orElse(null);
                if (tag != null) {
                    GroupTagMap tagMap = new GroupTagMap();
                    tagMap.setGroup(group);
                    tagMap.setTag(tag);
                    tagMapRepository.save(tagMap);
                }
            }
        }

        return mapToGroupResponseDto(group, user);
    }

    /**
     * Join a group
     */
    @Transactional
    public GroupMembershipResponseDto joinGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already member
        if (membershipRepository.existsByUserAndGroupAndStatus(user, group, MembershipStatus.ACTIVE)) {
            throw new IllegalStateException("Already a member of this group");
        }

        // Check if pending
        if (membershipRepository.existsByUserAndGroupAndStatus(user, group, MembershipStatus.PENDING)) {
            throw new IllegalStateException("Membership request already pending");
        }

        GroupMembership membership = new GroupMembership();
        membership.setUser(user);
        membership.setGroup(group);
        membership.setIsAdmin(false);

        // Set status based on group approval requirement
        if (group.getRequiresApproval()) {
            membership.setStatus(MembershipStatus.PENDING);
        } else {
            membership.setStatus(MembershipStatus.ACTIVE);
        }

        membership = membershipRepository.save(membership);
        log.info("User {} joined group {}", userId, groupId);

        return mapToMembershipResponseDto(membership);
    }

    /**
     * Leave a group
     */
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Cannot leave if creator
        if (group.getCreatedBy().getId().equals(userId)) {
            throw new IllegalStateException("Group creator cannot leave the group");
        }

        GroupMembership membership = membershipRepository.findByUserAndGroup(user, group)
                .orElseThrow(() -> new RuntimeException("Not a member of this group"));

        membershipRepository.delete(membership);
        log.info("User {} left group {}", userId, groupId);
    }

    /**
     * Get group details
     */
    public GroupResponseDto getGroup(Long groupId, Long currentUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToGroupResponseDto(group, currentUser);
    }

    /**
     * Get all active groups
     */
    public List<GroupResponseDto> getAllGroups(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Group> groups = groupRepository.findByIsActiveTrue();
        return groups.stream()
                .map(group -> mapToGroupResponseDto(group, currentUser))
                .collect(Collectors.toList());
    }

    /**
     * Get groups user is member of
     */
    public List<GroupResponseDto> getUserGroups(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<GroupMembership> memberships = membershipRepository.findByUserAndStatus(user, MembershipStatus.ACTIVE);

        return memberships.stream()
                .map(m -> mapToGroupResponseDto(m.getGroup(), user))
                .collect(Collectors.toList());
    }

    /**
     * Get group members
     */
    public List<GroupMembershipResponseDto> getGroupMembers(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        List<GroupMembership> memberships = membershipRepository.findByGroupAndStatus(group, MembershipStatus.ACTIVE);

        return memberships.stream()
                .map(this::mapToMembershipResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Add event to group
     */
    @Transactional
    public void addEventToGroup(Long groupId, Long eventId, Long userId, Boolean isPrivate) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is creator or admin of group
        GroupMembership membership = membershipRepository.findByUserAndGroup(user, group)
                .orElseThrow(() -> new RuntimeException("Not a member of this group"));

        if (!membership.getIsAdmin() && !group.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Only group admins can add events");
        }

        // Check if event already added
        if (eventMapRepository.existsByGroupAndEvent(group, event)) {
            throw new IllegalStateException("Event already added to this group");
        }

        GroupEventMap eventMap = new GroupEventMap();
        eventMap.setGroup(group);
        eventMap.setEvent(event);
        eventMap.setIsPrivate(isPrivate != null ? isPrivate : false);

        eventMapRepository.save(eventMap);
        log.info("Event {} added to group {}", eventId, groupId);
    }

    /**
     * Get events for a group
     */
    public List<EventResponseDto> getGroupEvents(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is member for private events
        boolean isMember = membershipRepository.existsByUserAndGroupAndStatus(user, group, MembershipStatus.ACTIVE);

        List<GroupEventMap> eventMaps = eventMapRepository.findByGroup(group);

        return eventMaps.stream()
                .filter(em -> !em.getIsPrivate() || isMember) // Filter private events for non-members
                .map(em -> mapToEventResponseDto(em.getEvent()))
                .collect(Collectors.toList());
    }

    /**
     * Map Group to GroupResponseDto
     */
    private GroupResponseDto mapToGroupResponseDto(Group group, User currentUser) {
        GroupResponseDto dto = new GroupResponseDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setGroupImageUrl(group.getGroupImageUrl());
        dto.setCreatorId(group.getCreatedBy().getId());
        dto.setCreatorName(group.getCreatedBy().getFullName());
        dto.setRequiresApproval(group.getRequiresApproval());
        dto.setIsActive(group.getIsActive());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());

        // Get member count
        long memberCount = membershipRepository.countByGroupAndStatus(group, MembershipStatus.ACTIVE);
        dto.setMemberCount(memberCount);

        // Get tags
        List<String> tags = tagMapRepository.findByGroup(group).stream()
                .map(tm -> tm.getTag().getDisplayName())
                .collect(Collectors.toList());
        dto.setTags(tags);

        // Check if current user is member
        boolean isMember = membershipRepository.existsByUserAndGroupAndStatus(currentUser, group, MembershipStatus.ACTIVE);
        dto.setIsMember(isMember);

        // Check if current user is creator
        dto.setIsCreator(group.getCreatedBy().getId().equals(currentUser.getId()));

        return dto;
    }

    /**
     * Map GroupMembership to GroupMembershipResponseDto
     */
    private GroupMembershipResponseDto mapToMembershipResponseDto(GroupMembership membership) {
        GroupMembershipResponseDto dto = new GroupMembershipResponseDto();
        dto.setId(membership.getId());
        dto.setUserId(membership.getUser().getId());
        dto.setUserFullName(membership.getUser().getFullName());
        dto.setUserEmail(membership.getUser().getEmail());
        dto.setUserProfileImageUrl(membership.getUser().getProfileImageUrl());
        dto.setGroupId(membership.getGroup().getId());
        dto.setGroupName(membership.getGroup().getName());
        dto.setStatus(membership.getStatus());
        dto.setIsAdmin(membership.getIsAdmin());
        dto.setJoinedAt(membership.getJoinedAt());
        return dto;
    }

    /**
     * Map Event to EventResponseDto
     */
    private EventResponseDto mapToEventResponseDto(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setVenue(event.getVenue());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setEventStatus(event.getEventStatus().toString());
        dto.setEventImageUrl(event.getEventImageUrl());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setIsPaid(event.getIsPaid());
        dto.setPrice(event.getPrice());
        dto.setAvailableSeats(event.getAvailableSeats());
        dto.setBookedSeats(event.getBookedSeats());
        return dto;
    }
}
