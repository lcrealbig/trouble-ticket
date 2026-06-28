package com.example.ticketer.persistence.repository;

import com.example.ticketer.persistence.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, String> {
    Optional<TenantEntity> findById(String id);

    Optional<TenantEntity> findByName(String tenantName);
}