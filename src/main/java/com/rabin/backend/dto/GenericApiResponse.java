package com.rabin.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Standard API response wrapper")
public class GenericApiResponse<T> {
    @Schema(description = "HTTP status code", example = "200")
    private int status;

    @Schema(description = "Response message", example = "Operation successful")
    private String message;

    @Schema(description = "Response data payload")
    private T data;

    public static <T> GenericApiResponse<T> ok(int status, String message, T data) {
        return new GenericApiResponse<>(status, message, data);
    }

    public static <T> GenericApiResponse<T> error(int status, String message) {
        return new GenericApiResponse<>(status, message, null);
    }
}
