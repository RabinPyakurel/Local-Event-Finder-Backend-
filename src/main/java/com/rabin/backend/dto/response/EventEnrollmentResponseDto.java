package com.rabin.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventEnrollmentResponseDto {
    private Long eventId;
    private String eventTitle;
    private LocalDateTime enrolledAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String venue;
    private Long userId;
    private String userFullName;
    private String userEmail;
}
