package com.example.ticketer.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record TenantResponse(
        String id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}