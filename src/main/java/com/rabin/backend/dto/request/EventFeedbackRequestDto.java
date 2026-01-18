package com.rabin.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventFeedbackRequestDto {
    private Long eventId;
    private Integer rating;
    private String comment;
}
