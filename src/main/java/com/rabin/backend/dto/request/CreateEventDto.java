package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Create or update event request payload")
public class CreateEventDto {
    @Schema(description = "Event title", example = "Tech Conference 2024", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Event description", example = "Annual technology conference featuring the latest innovations", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "Event venue/location name", example = "Kathmandu Convention Center", requiredMode = Schema.RequiredMode.REQUIRED)
    private String venue;

    @Schema(description = "Event image file")
    private MultipartFile eventImage;

    @Schema(description = "Event start date and time", example = "2024-06-15T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startDate;

    @Schema(description = "Event end date and time", example = "2024-06-15T18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endDate;

    @Schema(description = "Venue latitude for location-based search", example = "27.7172")
    private Double latitude;

    @Schema(description = "Venue longitude for location-based search", example = "85.3240")
    private Double longitude;

    @Schema(description = "List of event tags (InterestCategory names)", example = "[\"TECHNOLOGY\", \"NETWORKING\"]")
    private List<String> tags;

    @Schema(description = "Is this a paid event?", example = "true")
    private Boolean isPaid;

    @Schema(description = "Ticket price in NPR (required if isPaid is true)", example = "500.00")
    private Double price;

    @Schema(description = "Number of available seats (null for unlimited)", example = "100")
    private Integer availableSeats;
}
