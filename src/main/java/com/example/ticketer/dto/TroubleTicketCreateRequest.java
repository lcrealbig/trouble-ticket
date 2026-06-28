package com.example.ticketer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
public record TroubleTicketCreateRequest(@NotBlank(message = "externalId is required")
                                         String externalId,
                                         @NotNull(message = "serviceId is required") @Min(value = 1)
                                         Long serviceId,
                                         @NotBlank(message = "description is required")
                                         String description,
                                         @NotBlank(message = "status is required")
                                         String status,
                                         @NotBlank(message = "note is required")
                                         String note) {
}