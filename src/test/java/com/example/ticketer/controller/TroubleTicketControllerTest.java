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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

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

        TroubleTicketResponse response = new TroubleTicketResponse( "1", "EXT-001", 123L, "Test description", "acknowledged", null, null, null);

        when(troubleTicketService.createTroubleTicket(any())).thenReturn(response);
        when(troubleTicketService.isExisting(any())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/troubleTicket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Tenant-Id", "tenant-123"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.externalId").value("EXT-001"));
    }

    @Test
    void createTroubleTicket_ShouldReturnOkForIdempotency() throws Exception {
        // Given
        TroubleTicketCreateRequest request = new TroubleTicketCreateRequest("EXT-001", 123L, "Test description", "new", "Test note");

        TroubleTicketResponse response = new TroubleTicketResponse( "1", "EXT-001", null, null, null, null, null, null);

        when(troubleTicketService.createTroubleTicket(any())).thenReturn(response);
        when(troubleTicketService.isExisting(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/troubleTicket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Tenant-Id", "tenant-123"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void listTroubleTickets_ShouldReturnList() throws Exception {
        // Given
        TroubleTicketSummary summary1 = new TroubleTicketSummary("EXT-001", 123L, "Test description", "new");

        TroubleTicketSummary summary2 = new TroubleTicketSummary("EXT-002", 456L, "Another description", "acknowledged");

        Page<TroubleTicketSummary> pageResult = new PageImpl<>(List.of(summary1, summary2));
        when(troubleTicketService.listTroubleTickets(any(PageRequest.class))).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/troubleTicket")
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
        TroubleTicketResponse response = new TroubleTicketResponse("1", "EXT-001", 123L, "Test description", "new", null, null, null);

        when(troubleTicketService.getTroubleTicketByExternalId("1")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/troubleTicket/1")
                        .header("X-Tenant-Id", "tenant-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.externalId").value("EXT-001"));
    }

    @Test
    void closeTroubleTicket_ShouldReturnOk() throws Exception {
        // Given
        TroubleTicketCloseStatusRequest request = new TroubleTicketCloseStatusRequest("closed");

        TroubleTicketResponse response = new TroubleTicketResponse("1", "EXT-001", null, null, "closed", null, null, null);

        when(troubleTicketService.closeTroubleTicket("1", request)).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/troubleTicket/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Tenant-Id", "tenant-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("closed"));
    }
}