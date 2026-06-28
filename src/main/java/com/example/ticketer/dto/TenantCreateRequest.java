package com.example.ticketer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantCreateRequest(
        @NotBlank
        @Size(max = 50)
        String id,
        
        @NotBlank
        @Size(max = 255)
        String name) {
}