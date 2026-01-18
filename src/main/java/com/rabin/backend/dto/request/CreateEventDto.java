package com.rabin.backend.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CreateEventDto {
    private String title;
    private String description;
    private String venue;  // Event venue/location name
    private MultipartFile eventImage;  // Event image file upload
    private LocalDateTime eventDate;
    private Double latitude;
    private Double longitude;
    private List<String> tags;  // List of InterestCategory enum names

    // Paid event fields
    private Boolean isPaid;  // Is this a paid event?
    private Double price;  // Price in NPR
    private Integer availableSeats;  // Null = unlimited
}
