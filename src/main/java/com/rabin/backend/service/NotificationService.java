package com.rabin.backend.service;

import com.rabin.backend.dto.response.NotificationResponseDto;
import com.rabin.backend.enums.NotificationType;
import com.rabin.backend.exception.ResourceNotFoundException;
import com.rabin.backend.model.Notification;
import com.rabin.backend.model.User;
import com.rabin.backend.repository.NotificationRepository;
import com.rabin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendNotification(Long recipientId, NotificationType type,
                                  String title, String message,
                                  Long relatedEntityId, String relatedEntityType) {
        User recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            log.warn("Cannot send notification: recipient {} not found", recipientId);
            return;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);

        notificationRepository.save(notification);
        log.info("Notification saved: type={}, recipientId={}, entityId={}", type, recipientId, relatedEntityId);
    }

    public Page<NotificationResponseDto> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipient_IdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new IllegalStateException("You can only mark your own notifications as read");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        return mapToDto(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new IllegalStateException("You can only delete your own notifications");
        }

        notificationRepository.delete(notification);
    }

    private NotificationResponseDto mapToDto(Notification notification) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
