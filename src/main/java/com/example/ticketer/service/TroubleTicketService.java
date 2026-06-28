package com.example.ticketer.service;


import com.example.ticketer.dto.*;
import com.example.ticketer.exception.BadRequestException;
import com.example.ticketer.exception.TroubleTicketNotFoundException;
import com.example.ticketer.exception.UnauthorizedException;
import com.example.ticketer.mapper.TroubleTicketMapper;
import com.example.ticketer.persistence.entity.TenantEntity;
import com.example.ticketer.persistence.entity.TenantEntity;
import com.example.ticketer.persistence.entity.TicketStatus;
import com.example.ticketer.persistence.entity.TroubleTicketEntity;
import com.example.ticketer.persistence.repository.TenantRepository;
import com.example.ticketer.persistence.repository.TroubleTicketRepository;
import com.example.ticketer.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static org.springframework.http.HttpStatus.CREATED;


@Slf4j
@Service
@RequiredArgsConstructor
public class TroubleTicketService {

    private final NoteService noteService;
    private final TroubleTicketMapper mapper;
    private final TenantContext tenantContext;
    private final TenantRepository tenantRepository;
    private final TroubleTicketRepository troubleTicketRepository;


    private String validateTenantContext() {
        var tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("Unauthorized access attempt - tenant context not available");
            throw new UnauthorizedException("Tenant context not available");
        }
        return tenantId;
    }

    private TenantEntity findTenantById(String tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "Tenant not found: " + tenantId));
    }

    private TroubleTicketEntity findTicketByExternalId(String externalId, TenantEntity tenant) {
        return troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "TroubleTicket not found or not accessible: " + externalId));
    }

    @Transactional
    public ResponseEntity<TroubleTicketResponse> createTroubleTicket(TroubleTicketCreateRequest request) {
        var tenant = findTenantById(validateTenantContext());
        var existing = troubleTicketRepository
                .findByExternalIdAndTenant(request.externalId(), tenant)
                .orElse(null);

        if (existing != null) {
           var location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{externalId}")
                    .buildAndExpand(existing.getExternalId())
                    .toUri();

            log.info("Idempotent request processed: TroubleTicket with externalId={} already exists",
                    request.externalId());
            return ResponseEntity.ok()
                    .header("Location", location.toString())
                    .body(mapper.toResponse(existing));
        }

        if (!"new".equalsIgnoreCase(request.status())) {
            throw new BadRequestException("Create status must be 'new'");
        }


        var initialStatus = TicketStatus.ACKNOWLEDGED;
        var entity = mapper.toEntity(request);
        entity.setStatus(initialStatus);
        entity.setTenant(tenant);

        var ticket = troubleTicketRepository.save(entity);

        noteService.addNoteToTicket(ticket.getExternalId(), new NoteCreateRequest(request.note()), tenant.getId());

        log.info("Created TroubleTicket: externalId={}",
                ticket.getExternalId());

        return ResponseEntity.status(CREATED).body(mapper.toResponse(ticket));
    }

    @Transactional(readOnly = true)
    public Page<TroubleTicketSummary> listTroubleTickets(Pageable pageable) {

        validateTenantContext();

        var tickets = troubleTicketRepository.findAll(pageable);
        return tickets.map(mapper::toSummary);
    }

    @Transactional(readOnly = true)
    public TroubleTicketResponse getTroubleTicketByExternalId(String externalId) {
        var tenantId = validateTenantContext();
        var tenant = findTenantById(tenantId);
        var ticket = findTicketByExternalId(externalId, tenant);

        return mapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TroubleTicketResponse getTroubleTicketById(Long id) {
        var tenantId = validateTenantContext();
        var tenant = findTenantById(tenantId);

        var ticket = troubleTicketRepository.findByIdAndTenant(id, tenant)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "TroubleTicket not found or not accessible: " + id));

        return mapper.toResponse(ticket);
    }

    @Transactional
    public TroubleTicketResponse closeTroubleTicket(String externalId, TroubleTicketCloseStatusRequest request) {
        var tenantId = validateTenantContext();
        var tenant = findTenantById(tenantId);
        var ticket = findTicketByExternalId(externalId, tenant);

        if (!"closed".equalsIgnoreCase(request.status())) {
            throw new BadRequestException("Public update only allows 'closed' status");
        }

        ticket.setStatus(TicketStatus.CLOSED);
        ticket = troubleTicketRepository.save(ticket);

        log.info("Closed TroubleTicket: externalId={}", externalId);

        return mapper.toResponse(ticket);
    }

    public boolean isExisting(String externalId) {
        var tenantId = tenantContext.getTenantId();
        if (tenantId == null) return false;
        var tenant = findTenantById(tenantId);

        return troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant).isPresent();
    }

    @Transactional(readOnly = true)
    public Long getTicketIdByExternalId(String externalId) {
        var tenantId = validateTenantContext();
        var tenant = findTenantById(tenantId);
        var ticket = findTicketByExternalId(externalId, tenant);

        return ticket.getId();
    }

}