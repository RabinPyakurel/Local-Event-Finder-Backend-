package com.rabin.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> GenericApiResponse<T> ok(int status, String message, T data) {
        return new GenericApiResponse<>(status, message, data);
    }

    public static <T> GenericApiResponse<T> error(int status, String message) {
        return new GenericApiResponse<>(status, message, null);
    }
}
