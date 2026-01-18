package com.rabin.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportResponseDto {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;
    private Long eventId;
    private String eventTitle;
    private String reason;
    private String reportStatus;
    private LocalDateTime createdAt;
}
