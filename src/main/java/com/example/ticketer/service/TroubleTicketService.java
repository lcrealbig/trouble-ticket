package com.example.ticketer.service;


import com.example.ticketer.dto.*;
import com.example.ticketer.exception.BadRequestException;
import com.example.ticketer.exception.TroubleTicketNotFoundException;
import com.example.ticketer.exception.UnauthorizedException;
import com.example.ticketer.mapper.TroubleTicketMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class TroubleTicketService {

    private final NoteService noteService;
    private final TroubleTicketMapper mapper;
    private final TenantContext tenantContext;
    private final TenantRepository tenantRepository;
    private final TroubleTicketRepository troubleTicketRepository;

    private static final String CACHE_NAME = "troubleTickets";

    @Transactional
    public TroubleTicketResponse createTroubleTicket(TroubleTicketCreateRequest request) {
        var tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            throw new UnauthorizedException("Tenant context not available");
        }

        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "Tenant not found: " + tenantId));
        var existing = troubleTicketRepository
                .findByExternalIdAndTenant(request.externalId(), tenant)
                .orElse(null);

        if (existing != null) {

            log.info("Idempotent request: TroubleTicket with externalId={} already exists for tenant={}",
                    request.externalId(), tenantId);
            return mapper.toResponse(existing);
        }

        if (!"new".equalsIgnoreCase(request.status())) {
            throw new BadRequestException("Create status must be 'new'");
        }
        

        var initialStatus = TicketStatus.ACKNOWLEDGED;
        var entity = mapper.toEntity(request);
        entity.setStatus(initialStatus);
        entity.setTenant(tenant);
        
        var ticket = troubleTicketRepository.save(entity);

        noteService.addNoteToTicket(ticket.getExternalId(), new NoteCreateRequest(request.note()), tenantId);

        log.info("Created TroubleTicket: id={}, externalId={}, tenant={}",
                ticket.getId(), ticket.getExternalId(), tenantId);

        return mapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TroubleTicketSummary> listTroubleTickets(Pageable pageable) {
        var tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("");
            throw new UnauthorizedException("Tenant context not available");
        }
        var tickets = troubleTicketRepository.findAll(pageable);
        return tickets.map(mapper::toSummary);
    }

    @Cacheable(value = CACHE_NAME, key = "#externalId + '-' + #tenantContext.getTenantId()")
    @Transactional(readOnly = true)
    public TroubleTicketResponse getTroubleTicketByExternalId(String externalId) {
        var tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            throw new UnauthorizedException("Tenant context not available");
        }
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "Tenant not found: " + tenantId));

        var ticket = troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "TroubleTicket not found or not accessible: " + externalId));

        return mapper.toResponse(ticket);
    }

    @Cacheable(value = CACHE_NAME, key = "#id + '-' + #tenantContext.getTenantId()")
    @Transactional(readOnly = true)
    public TroubleTicketResponse getTroubleTicketById(Long id) {
        var tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            throw new UnauthorizedException("Tenant context not available");
        }
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "Tenant not found: " + tenantId));

        var ticket = troubleTicketRepository.findByIdAndTenant(id, tenant)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "TroubleTicket not found or not accessible: " + id));

        return mapper.toResponse(ticket);
    }

    @Transactional
    public TroubleTicketResponse closeTroubleTicket(String externalId, TroubleTicketCloseStatusRequest request) {
        var tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            throw new UnauthorizedException("Tenant context not available");
        }
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "Tenant not found: " + tenantId));
        var ticket = troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "TroubleTicket not found or not accessible: " + externalId));

        if (!"closed".equalsIgnoreCase(request.status())) {
            throw new BadRequestException("Public update only allows 'closed' status");
        }

        ticket.setStatus(TicketStatus.CLOSED);
        ticket = troubleTicketRepository.save(ticket);

        log.info("Closed TroubleTicket: externalId={}, tenant={}", externalId, tenantId);

        return mapper.toResponse(ticket);
    }

    public boolean isExisting(String externalId) {
        var tenantId = tenantContext.getTenantId();
        if (tenantId == null) return false;
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "Tenant not found: " + tenantId));
        
        return troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant).isPresent();
    }

    public String getCurrentTenant() {
        return tenantContext.getTenantId();
    }
    
    @Transactional(readOnly = true)
    public Long getTicketIdByExternalId(String externalId) {
        var tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            throw new UnauthorizedException("Tenant context not available");
        }
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "Tenant not found: " + tenantId));
        
        return troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)
                .map(TroubleTicketEntity::getId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "TroubleTicket not found or not accessible: " + externalId));
    }

}