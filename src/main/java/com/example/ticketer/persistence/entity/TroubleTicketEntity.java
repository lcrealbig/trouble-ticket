package com.example.ticketer.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trouble_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TroubleTicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trouble_ticket_id_seq")
    @SequenceGenerator(name = "trouble_ticket_id_seq", sequenceName = "trouble_ticket_id_seq", allocationSize = 1)
    private Long id;
    
    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @OneToMany(mappedBy = "troubleTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NoteEntity> notes = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public void addNote(NoteEntity note) {
        notes.add(note);
        note.setTroubleTicket(this);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}