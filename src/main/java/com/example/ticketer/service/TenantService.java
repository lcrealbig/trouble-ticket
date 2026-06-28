package com.example.ticketer.service;

import com.example.ticketer.exception.TenantNotFoundException;
import com.example.ticketer.persistence.entity.TenantEntity;
import com.example.ticketer.persistence.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantEntity getTenantById(String tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
    }

    public TenantEntity getTenantByName(String tenantName) {
        return tenantRepository.findByName(tenantName)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantName));
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
        return tenantRepository.save(tenant);
    }

    public TenantEntity updateTenant(TenantEntity tenant) {
        return tenantRepository.save(tenant);
    }

    public void deleteTenant(String tenantId) {
        tenantRepository.deleteById(tenantId);
    }
}