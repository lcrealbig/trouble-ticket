package com.example.ticketer.controller;


import com.example.ticketer.dto.TroubleTicketCloseStatusRequest;
import com.example.ticketer.dto.TroubleTicketCreateRequest;
import com.example.ticketer.dto.TroubleTicketResponse;
import com.example.ticketer.dto.TroubleTicketSummary;
import com.example.ticketer.service.TroubleTicketService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("api/v1/troubleTicket")
@RequiredArgsConstructor
@Slf4j
@Timed("trouble.ticket.controller")
public class TroubleTicketController {

    private final TroubleTicketService troubleTicketService;

    @PostMapping
    public ResponseEntity<TroubleTicketResponse> createTroubleTicket(
            @Valid @RequestBody TroubleTicketCreateRequest request
    ) {
        log.info("Creating trouble ticket with externalId: {}", request.externalId());
        var response = troubleTicketService.createTroubleTicket(request);

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{externalId}")
                .buildAndExpand(response.externalId())
                .toUri();

        var isExisting = troubleTicketService.isExisting(response.externalId());

        if (isExisting) {
            return ResponseEntity.ok()
                    .header("Location", location.toString())
                    .body(response);
        } else {
            return ResponseEntity.created(location)
                    .body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Page<TroubleTicketSummary>> listTroubleTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Listing trouble tickets with pagination - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        var summaries = troubleTicketService.listTroubleTickets(pageable);
        log.debug("Found {} trouble tickets", summaries.getTotalElements());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<TroubleTicketResponse> getTroubleTicketByExternalId(
            @PathVariable String externalId
    ) {
        log.info("Getting trouble ticket with externalId: {}", externalId);
        var response = troubleTicketService.getTroubleTicketByExternalId(externalId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{externalId}")
    public ResponseEntity<TroubleTicketResponse> closeTroubleTicket(
            @PathVariable String externalId,
            @Valid @RequestBody TroubleTicketCloseStatusRequest request
    ) {
        log.info("Closing trouble ticket with externalId: {}, status: {}", externalId, request.status());
        TroubleTicketResponse response = troubleTicketService.closeTroubleTicket(externalId, request);
        log.info("Successfully closed trouble ticket with externalId: {}", externalId);
        return ResponseEntity.ok(response);
    }
}