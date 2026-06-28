package com.example.ticketer.controller;

import com.example.ticketer.dto.NoteCreateRequest;
import com.example.ticketer.dto.NoteResponse;
import com.example.ticketer.service.NoteService;
import com.example.ticketer.service.TroubleTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/troubleTicket")
public class NoteController {

    private final NoteService noteService;
    private final TroubleTicketService troubleTicketService;

    @PostMapping("/{externalId}/note")
    public ResponseEntity<NoteResponse> addNoteToTicket(
            @PathVariable String externalId,
            @Valid @RequestBody NoteCreateRequest request
    ) {
        var tenantId = troubleTicketService.getCurrentTenant();

        var response = noteService.addNoteToTicket(externalId, request, tenantId);

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{noteId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(response);
    }
}