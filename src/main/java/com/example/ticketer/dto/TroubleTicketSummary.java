package com.example.ticketer.dto;

import lombok.Builder;

@Builder
public record TroubleTicketSummary(String externalId,
                                   Long serviceId,
                                   String description,
                                   String status) {
}