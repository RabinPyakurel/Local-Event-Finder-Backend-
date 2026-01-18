package com.rabin.backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateInterestsDto {
    private List<String> interests;  // List of InterestCategory enum names
}
