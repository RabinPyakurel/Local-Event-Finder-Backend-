package com.rabin.backend.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Request DTO for creating or updating a group
 */
@Getter
@Setter
public class GroupRequestDto {
    private String name;
    private String description;
    private MultipartFile groupImage;
    private Boolean requiresApproval;
    private List<String> tags;  // List of tag names or interest categories
}
