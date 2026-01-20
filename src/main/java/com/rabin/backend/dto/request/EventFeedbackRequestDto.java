package com.rabin.backend.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class EventFeedbackRequestDto {
    private Long eventId;
    private Integer rating;
    private String comment;
}
