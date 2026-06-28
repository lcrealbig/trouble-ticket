package com.example.ticketer.mapper;


import com.example.ticketer.dto.*;
import com.example.ticketer.persistence.entity.NoteEntity;
import com.example.ticketer.persistence.entity.TenantEntity;
import com.example.ticketer.persistence.entity.TicketStatus;
import com.example.ticketer.persistence.entity.TroubleTicketEntity;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TroubleTicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "status", source = "request.status", qualifiedByName = "mapCreateStatus")
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TroubleTicketEntity toEntity(TroubleTicketCreateRequest request);

    @Named("mapCreateStatus")
    default TicketStatus mapCreateStatus(String status) {
        return TicketStatus.valueOf(status.toUpperCase());
    }

    @Mapping(target = "notes", source = "troubleTicket.notes")
    TroubleTicketResponse toResponse(TroubleTicketEntity troubleTicket);

    List<TroubleTicketSummary> toSummaryList(List<TroubleTicketEntity> troubleTickets);
    TroubleTicketSummary toSummary(TroubleTicketEntity troubleTicket);

    @Mapping(target = "troubleTicket", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    NoteEntity toEntity(NoteCreateRequest request);

    NoteResponse toNoteResponse(NoteEntity note);
    List<NoteResponse> toNoteResponseList(List<NoteEntity> notes);

    TenantResponse toTenantResponse(TenantEntity tenant);
    TenantEntity toTenantEntity(TenantCreateRequest request);
}