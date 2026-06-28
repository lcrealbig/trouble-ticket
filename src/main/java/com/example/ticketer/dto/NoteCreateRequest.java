package com.example.ticketer.dto;

import jakarta.validation.constraints.NotBlank;

public record NoteCreateRequest(
        @NotBlank(message = "text is required") String text
) {
}

