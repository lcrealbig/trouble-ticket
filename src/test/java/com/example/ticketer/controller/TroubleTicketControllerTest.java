package com.example.ticketer.controller;

import com.example.ticketer.dto.TroubleTicketCloseStatusRequest;
import com.example.ticketer.dto.TroubleTicketCreateRequest;
import com.example.ticketer.dto.TroubleTicketResponse;
import com.example.ticketer.dto.TroubleTicketSummary;
import com.example.ticketer.service.TroubleTicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.ResponseEntity;
import java.util.List;
import static org.springframework.http.HttpStatus.CREATED;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TroubleTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TroubleTicketService troubleTicketService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTroubleTicket_ShouldReturnCreated() throws Exception {
        // Given
        TroubleTicketCreateRequest request = new TroubleTicketCreateRequest("EXT-001", 123L, "Test description", "new", "Test note");

        TroubleTicketResponse response = new TroubleTicketResponse("EXT-001", 123L, "Test description", "acknowledged", null, null, null);

        when(troubleTicketService.createTroubleTicket(any())).thenReturn(ResponseEntity.status(CREATED)
                .header("Location", "/api/v1/troubleTicket/EXT-001")
                .body(response));
        when(troubleTicketService.isExisting(any())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/troubleTicket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Tenant-Id", "tenant-123"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.externalId").value("EXT-001"))
                .andExpect(jsonPath("$.serviceId").value(123))
                .andExpect(jsonPath("$.status").value("acknowledged"));
    }

    @Test
    void createTroubleTicket_ShouldReturnOkForIdempotency() throws Exception {
        // Given
        TroubleTicketCreateRequest request = new TroubleTicketCreateRequest("EXT-001", 123L, "Test description", "new", "Test note");

        TroubleTicketResponse response = new TroubleTicketResponse("EXT-001", 123L, "Test description", "acknowledged", null, null, null);

        when(troubleTicketService.createTroubleTicket(any())).thenReturn(ResponseEntity.ok()
                .header("Location", "/api/v1/troubleTicket/EXT-001")
                .body(response));
        when(troubleTicketService.isExisting(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/troubleTicket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Tenant-Id", "tenant-123"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.externalId").value("EXT-001"));
    }

    @Test
    void listTroubleTickets_ShouldReturnList() throws Exception {
        // Given
        TroubleTicketSummary summary1 = new TroubleTicketSummary("EXT-001", 123L, "Test description", "new");

        TroubleTicketSummary summary2 = new TroubleTicketSummary("EXT-002", 456L, "Another description", "acknowledged");

        Page<TroubleTicketSummary> pageResult = new PageImpl<>(List.of(summary1, summary2));
        when(troubleTicketService.listTroubleTickets(any(PageRequest.class))).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/troubleTicket")
                        .header("X-Tenant-Id", "tenant-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].externalId").value("EXT-001"))
                .andExpect(jsonPath("$.content[1].externalId").value("EXT-002"));
    }

    @Test
    void getTroubleTicketById_ShouldReturnTicket() throws Exception {
        // Given
        TroubleTicketResponse response = new TroubleTicketResponse("EXT-001", 123L, "Test description", "new", null, null, null);

        when(troubleTicketService.getTroubleTicketByExternalId("EXT-001")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/troubleTicket/EXT-001")
                        .header("X-Tenant-Id", "tenant-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("EXT-001"))
                .andExpect(jsonPath("$.serviceId").value(123));
    }

    @Test
    void closeTroubleTicket_ShouldReturnOk() throws Exception {
        // Given
        TroubleTicketCloseStatusRequest request = new TroubleTicketCloseStatusRequest("closed");

        TroubleTicketResponse response = new TroubleTicketResponse("EXT-001", 123L, "Test description", "closed", null, null, null);

        when(troubleTicketService.closeTroubleTicket("EXT-001", request)).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/troubleTicket/EXT-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Tenant-Id", "tenant-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("EXT-001"))
                .andExpect(jsonPath("$.status").value("closed"));
    }
}