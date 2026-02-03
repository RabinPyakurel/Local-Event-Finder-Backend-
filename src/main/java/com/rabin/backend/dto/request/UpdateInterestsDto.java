package com.rabin.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Update user interests request payload")
public class UpdateInterestsDto {
    @Schema(description = "List of interest category names", example = "[\"MUSIC\", \"SPORTS\", \"TECHNOLOGY\", \"FOOD\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> interests;
}
