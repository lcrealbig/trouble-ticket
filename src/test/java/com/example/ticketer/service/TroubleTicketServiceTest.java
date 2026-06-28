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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TroubleTicketServiceTest {

    @Mock
    private TroubleTicketRepository troubleTicketRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private NoteService noteService;

    @Mock
    private TroubleTicketMapper mapper;

    @Mock
    private TenantContext tenantContext;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TroubleTicketService troubleTicketService;

    private final String TEST_TENANT = "tenant-123";

    @BeforeEach
    void setUp() {
        when(tenantContext.getTenantId()).thenReturn(TEST_TENANT);
    }

    @Test
    void createTroubleTicket_ShouldCreateNewTicketSuccessfully() {
        // Given
        var request = new TroubleTicketCreateRequest("EXT-001", 123L, "Test description", "new", "Test note");

        var tenant = new TenantEntity();
        tenant.setId(TEST_TENANT);
        tenant.setName("Test Tenant");

        var expectedTicket = new TroubleTicketEntity();
        expectedTicket.setId(1L);
        expectedTicket.setExternalId("EXT-001");
        expectedTicket.setTenant(tenant);

        var expectedResponse = new TroubleTicketResponse( "EXT-001", 123L, "Test description", "new", null, null, null);

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByExternalIdAndTenant("EXT-001", tenant)).thenReturn(Optional.empty());
        when(mapper.toEntity(request)).thenReturn(expectedTicket);
        when(troubleTicketRepository.save(expectedTicket)).thenReturn(expectedTicket);
        when(mapper.toResponse(expectedTicket)).thenReturn(expectedResponse);
        when(noteService.addNoteToTicket(anyString(), any(NoteCreateRequest.class), anyString())).thenReturn(new NoteResponse(1L, "Test note", OffsetDateTime.now(), OffsetDateTime.now()));

        // When
        var result = troubleTicketService.createTroubleTicket(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertEquals("EXT-001", result.getBody().externalId());
        verify(troubleTicketRepository, times(1)).save(expectedTicket);
        verify(noteService, times(1)).addNoteToTicket(anyString(), any(NoteCreateRequest.class), anyString());
    }

  
    @Test
    void createTroubleTicket_ShouldBeIdempotent() {
        // Given
        var request = new TroubleTicketCreateRequest("EXT-001", 123L, "Test description", "new", "Test note");

        var tenant = new TenantEntity();
        tenant.setId(TEST_TENANT);
        tenant.setName("Test Tenant");

        var existingTicket = new TroubleTicketEntity();
        existingTicket.setId(1L);
        existingTicket.setExternalId("EXT-001");
        existingTicket.setTenant(tenant);

        var expectedResponse = new TroubleTicketResponse("EXT-001", 123L, "Test description", "acknowledged", null, null, null);

        // Setup mock web context for ServletUriComponentsBuilder
        MockHttpServletRequest requestMock = new MockHttpServletRequest();
        requestMock.setServletPath("/api/v1/troubleTicket");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requestMock));

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByExternalIdAndTenant("EXT-001", tenant)).thenReturn(Optional.of(existingTicket));
        when(mapper.toResponse(existingTicket)).thenReturn(expectedResponse);

        // When
        var result = troubleTicketService.createTroubleTicket(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertEquals("EXT-001", result.getBody().externalId());
        assertEquals(200, result.getStatusCode().value()); // OK status for idempotent response
        verify(troubleTicketRepository, never()).save(any());
        verify(noteService, never()).addNoteToTicket(anyString(), any(NoteCreateRequest.class), anyString());
    }

    @Test
    void createTroubleTicket_ShouldThrowForInvalidStatus() {
        // Given
        var request = new TroubleTicketCreateRequest("EXT-001", 123L, "Test description", "closed", "Test note");

        var tenant = new TenantEntity();
        tenant.setId(TEST_TENANT);

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByExternalIdAndTenant("EXT-001", tenant)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            troubleTicketService.createTroubleTicket(request);
        });
    }

    @Test
    void createTroubleTicket_ShouldThrowWhenTenantNotFound() {
        // Given
        var request = new TroubleTicketCreateRequest("EXT-001", 123L, "Test description", "new", "Test note");

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TroubleTicketNotFoundException.class, () -> {
            troubleTicketService.createTroubleTicket(request);
        });
    }

    @Test
    void createTroubleTicket_ShouldThrowWhenTenantContextNotAvailable() {
        // Given
        var request = new TroubleTicketCreateRequest("EXT-001", 123L, "Test description", "new", "Test note");
        when(tenantContext.getTenantId()).thenReturn(null);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            troubleTicketService.createTroubleTicket(request);
        });
    }

    @Test
    void getTroubleTicketById_ShouldReturnTicketSuccessfully() {
        // Given
        var ticketId = 1L;
        var tenant = new TenantEntity();
        tenant.setId(TEST_TENANT);

        var ticket = new TroubleTicketEntity();
        ticket.setId(ticketId);
        ticket.setExternalId("EXT-001");
        ticket.setTenant(tenant);

        var expectedResponse = new TroubleTicketResponse( "EXT-001", 123L, "Test description", "new", null, null, null);

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByIdAndTenant(ticketId, tenant)).thenReturn(Optional.of(ticket));
        when(mapper.toResponse(ticket)).thenReturn(expectedResponse);

        // When
        var result = troubleTicketService.getTroubleTicketById(ticketId);

        // Then
        assertNotNull(result);
        assertEquals("EXT-001", result.externalId());
    }

    @Test
    void closeTroubleTicket_ShouldCloseTicketSuccessfully() {
        // Given
        var externalId = "EXT-001";
        TroubleTicketCloseStatusRequest request = new TroubleTicketCloseStatusRequest("closed");

        var tenant = new TenantEntity();
        tenant.setId(TEST_TENANT);

        var ticket = new TroubleTicketEntity();
        ticket.setId(1L);
        ticket.setExternalId(externalId);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setTenant(tenant);

        var closedTicket = new TroubleTicketEntity();
        closedTicket.setId(1L);
        closedTicket.setExternalId(externalId);
        closedTicket.setStatus(TicketStatus.CLOSED);
        closedTicket.setTenant(tenant);

        var expectedResponse = new TroubleTicketResponse( externalId, null, null, "closed", null, null, null);

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)).thenReturn(Optional.of(ticket));
        when(troubleTicketRepository.save(ticket)).thenReturn(closedTicket);
        when(mapper.toResponse(closedTicket)).thenReturn(expectedResponse);

        // When
        var result = troubleTicketService.closeTroubleTicket(externalId, request);

        // Then
        assertNotNull(result);
        assertEquals("closed", result.status());
        verify(troubleTicketRepository, times(1)).save(ticket);
    }

    @Test
    void closeTroubleTicket_ShouldThrowWhenStatusNotClosed() {
        // Given
        var externalId = "EXT-001";
        TroubleTicketCloseStatusRequest request = new TroubleTicketCloseStatusRequest("resolved");

        var tenant = new TenantEntity();;
        tenant.setId(TEST_TENANT);

        var ticket = new TroubleTicketEntity();
        ticket.setId(1L);
        ticket.setExternalId(externalId);
        ticket.setTenant(tenant);

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)).thenReturn(Optional.of(ticket));

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            troubleTicketService.closeTroubleTicket(externalId, request);
        });
    }

    @Test
    void listTroubleTickets_ShouldReturnList() {
        // Given
        var tenant = new TenantEntity();;
        tenant.setId(TEST_TENANT);
        tenant.setName("Test Tenant");

        var ticket1 = new TroubleTicketEntity();
        ticket1.setId(1L);
        ticket1.setExternalId("EXT-001");
        ticket1.setTenant(tenant);

        var ticket2 = new TroubleTicketEntity();
        ticket2.setId(2L);
        ticket2.setExternalId("EXT-002");
        ticket2.setTenant(tenant);

        TroubleTicketSummary summary1 = new TroubleTicketSummary("EXT-001", 123L, "Test description", "new");
        TroubleTicketSummary summary2 = new TroubleTicketSummary("EXT-002", 456L, "Another description", "acknowledged");

        when(troubleTicketRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket1, ticket2)));
        when(mapper.toSummary(ticket1)).thenReturn(summary1);
        when(mapper.toSummary(ticket2)).thenReturn(summary2);

        // When
        Page<TroubleTicketSummary> result = troubleTicketService.listTroubleTickets(PageRequest.of(0, 20));

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals("EXT-001", result.getContent().get(0).externalId());
        assertEquals("EXT-002", result.getContent().get(1).externalId());
    }

    @Test
    void listTroubleTickets_ShouldThrowWhenTenantContextNotAvailable() {
        // Given
        when(tenantContext.getTenantId()).thenReturn(null);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            troubleTicketService.listTroubleTickets(PageRequest.of(0, 20));
        });
    }

    @Test
    void getTroubleTicketById_ShouldThrowWhenTenantContextNotAvailable() {
        // Given
        Long ticketId = 1L;
        when(tenantContext.getTenantId()).thenReturn(null);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            troubleTicketService.getTroubleTicketById(ticketId);
        });
    }

    @Test
    void closeTroubleTicket_ShouldThrowWhenTenantContextNotAvailable() {
        // Given
        var externalId = "EXT-001";
        TroubleTicketCloseStatusRequest request = new TroubleTicketCloseStatusRequest("closed");
        when(tenantContext.getTenantId()).thenReturn(null);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            troubleTicketService.closeTroubleTicket(externalId, request);
        });
    }

    @Test
    void isExisting_ShouldReturnTrueWhenTicketExists() {
        // Given
        var externalId = "EXT-001";
        var tenant = new TenantEntity();
        tenant.setId(TEST_TENANT);

        var ticket = new TroubleTicketEntity();
        ticket.setExternalId(externalId);
        ticket.setTenant(tenant);

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)).thenReturn(Optional.of(ticket));

        // When
        boolean result = troubleTicketService.isExisting(externalId);

        // Then
        assertTrue(result);
    }

    @Test
    void isExisting_ShouldReturnFalseWhenTicketNotExists() {
        // Given
        var externalId = "EXT-999";
        var tenant = new TenantEntity();
        tenant.setId(TEST_TENANT);

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)).thenReturn(Optional.empty());

        // When
        boolean result = troubleTicketService.isExisting(externalId);

        // Then
        assertFalse(result);
    }

    @Test
    void isExisting_ShouldReturnFalseWhenTenantContextNotAvailable() {
        // Given
        var externalId = "EXT-001";
        when(tenantContext.getTenantId()).thenReturn(null);

        // When
        boolean result = troubleTicketService.isExisting(externalId);

        // Then
        assertFalse(result);
    }

    @Test
    void getTicketIdByExternalId_ShouldReturnIdSuccessfully() {
        // Given
        var externalId = "EXT-001";
        var tenant = new TenantEntity();
        tenant.setId(TEST_TENANT);

        var ticket = new TroubleTicketEntity();
        ticket.setId(123L);
        ticket.setExternalId(externalId);
        ticket.setTenant(tenant);

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)).thenReturn(Optional.of(ticket));

        // When
        Long result = troubleTicketService.getTicketIdByExternalId(externalId);

        // Then
        assertNotNull(result);
        assertEquals(123L, result);
    }

    @Test
    void getTicketIdByExternalId_ShouldThrowWhenTicketNotFound() {
        // Given
        var externalId = "EXT-999";
        var tenant = new TenantEntity();
        tenant.setId(TEST_TENANT);

        when(tenantRepository.findById(TEST_TENANT)).thenReturn(Optional.of(tenant));
        when(troubleTicketRepository.findByExternalIdAndTenant(externalId, tenant)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TroubleTicketNotFoundException.class, () -> {
            troubleTicketService.getTicketIdByExternalId(externalId);
        });
    }

    @Test
    void getTicketIdByExternalId_ShouldThrowWhenTenantContextNotAvailable() {
        // Given
        var externalId = "EXT-001";
        when(tenantContext.getTenantId()).thenReturn(null);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            troubleTicketService.getTicketIdByExternalId(externalId);
        });
    }
}