package com.example.ticketer.service;

import com.example.ticketer.exception.TenantNotFoundException;
import com.example.ticketer.persistence.entity.TenantEntity;
import com.example.ticketer.persistence.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantEntity getTenantById(String tenantId) {
        log.debug("Fetching tenant by ID: {}", tenantId);
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
        log.debug("Successfully retrieved tenant: {}", tenantId);
        return tenant;
    }

    public TenantEntity getTenantByName(String tenantName) {
        var tenant = tenantRepository.findByName(tenantName)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantName));
        return tenant;
    }

    public Optional<TenantEntity> findTenantById(String tenantId) {
        return tenantRepository.findById(tenantId);
    }

    public Optional<TenantEntity> findTenantName(String name) {
        return tenantRepository.findByName(name);
    }

    public List<TenantEntity> getAllTenants() {
        return tenantRepository.findAll();
    }

    public TenantEntity createTenant(TenantEntity tenant) {
        var savedTenant = tenantRepository.save(tenant);
        return savedTenant;
    }

    public TenantEntity updateTenant(TenantEntity tenant) {
        var updatedTenant = tenantRepository.save(tenant);
        return updatedTenant;
    }

    public void deleteTenant(String tenantId) {
        tenantRepository.deleteById(tenantId);
    }
}