package com.rabin.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventFeedbackResponseDto {
    private Long eventId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
