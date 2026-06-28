package com.example.ticketer.dto;

import lombok.*;

@Builder
public record ErrorResponse(String code,
                            String message,
                            String requestId) {
}