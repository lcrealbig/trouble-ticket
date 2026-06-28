package com.example.ticketer.dto;

import lombok.Builder;

/**
 * Note response DTO extending beyond OpenAPI specification.
 * Includes both createdAt and updatedAt timestamps for better audit trail.
 * This is a conscious decision to provide more complete data in responses.
 */
@Builder
public record NoteResponse(Long id,
                           String text,
                           java.time.OffsetDateTime createdAt,
                           java.time.OffsetDateTime updatedAt) {
}