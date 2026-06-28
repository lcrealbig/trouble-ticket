package com.example.ticketer.persistence.repository;

import com.example.ticketer.persistence.entity.TenantEntity;
import com.example.ticketer.persistence.entity.TroubleTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TroubleTicketRepository extends JpaRepository<TroubleTicketEntity, Long> {
    Optional<TroubleTicketEntity> findByExternalId(String externalId);
    Optional<TroubleTicketEntity> findByIdAndTenant(Long id, TenantEntity tenant);
    
    // Add method to find by externalId with tenant scope for proper idempotency
    Optional<TroubleTicketEntity> findByExternalIdAndTenant(String externalId, TenantEntity tenant);
}