package com.rabin.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EventResponseDto {
    private Long id;
    private String title;
    private String description;
    private String venue;
    private String eventImageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double latitude;
    private Double longitude;
    private String organizerName;
    private String eventStatus;
    private List<String> tags;
    private Double finalScore;  // For recommendations

    // Paid event fields
    private Boolean isPaid;
    private Double price;
    private Integer availableSeats;
    private Integer bookedSeats;
}
