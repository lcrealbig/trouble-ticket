package com.example.ticketer.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record TroubleTicketResponse(
        String id,
        String externalId,
        Long serviceId,
        String description,
        String status,
        List<NoteResponse> notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

}