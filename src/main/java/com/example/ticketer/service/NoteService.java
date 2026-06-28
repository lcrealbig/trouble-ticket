package com.example.ticketer.service;


import com.example.ticketer.dto.NoteCreateRequest;
import com.example.ticketer.dto.NoteResponse;
import com.example.ticketer.exception.TroubleTicketNotFoundException;
import com.example.ticketer.exception.UnauthorizedException;
import com.example.ticketer.mapper.TroubleTicketMapper;
import lombok.extern.slf4j.Slf4j;
import com.example.ticketer.persistence.repository.NoteRepository;
import com.example.ticketer.persistence.repository.TroubleTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {

    private final NoteRepository noteRepository;
    private final TroubleTicketRepository troubleTicketRepository;
    private final TroubleTicketMapper mapper;
    private static final String CACHE_NAME = "troubleTickets";

    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#externalId + '-' + #tenantId")
    public NoteResponse addNoteToTicket(String externalId, NoteCreateRequest request, String tenantId) {
        if (tenantId == null) {
            log.warn("Unauthorized attempt to add note - tenant context not available");
            throw new UnauthorizedException("Tenant context not available");
        }


        var ticket = troubleTicketRepository.findByExternalId(externalId)
                .orElseThrow(() -> new TroubleTicketNotFoundException(
                        "TroubleTicket not found or not accessible: " + externalId));

        var note = mapper.toEntity(request);
        note.setTroubleTicket(ticket);

        note = noteRepository.save(note);
        ticket.addNote(note);

        return mapper.toNoteResponse(note);
    }

}