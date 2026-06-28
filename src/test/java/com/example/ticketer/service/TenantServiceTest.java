package com.example.ticketer.service;

import com.example.ticketer.exception.TenantNotFoundException;
import com.example.ticketer.persistence.entity.TenantEntity;
import com.example.ticketer.persistence.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    private final String TEST_TENANT_ID = "test-tenant-123";

    @Test
    void getTenantById_ShouldReturnTenant() {
        // Given
        TenantEntity tenant = new TenantEntity();
        tenant.setId(TEST_TENANT_ID);
        tenant.setName("Test Tenant");

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));

        // When
        TenantEntity result = tenantService.getTenantById(TEST_TENANT_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_TENANT_ID, result.getId());
        assertEquals("Test Tenant", result.getName());
    }

    @Test
    void getTenantById_ShouldThrowWhenNotFound() {
        // Given
        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TenantNotFoundException.class, () -> {
            tenantService.getTenantById(TEST_TENANT_ID);
        });
    }

    @Test
    void findTenantById_ShouldReturnOptional() {
        // Given
        TenantEntity tenant = new TenantEntity();
        tenant.setId(TEST_TENANT_ID);

        when(tenantRepository.findById(TEST_TENANT_ID)).thenReturn(Optional.of(tenant));

        // When
        Optional<TenantEntity> result = tenantService.findTenantById(TEST_TENANT_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(TEST_TENANT_ID, result.get().getId());
    }

    @Test
    void getAllTenants_ShouldReturnList() {
        // Given
        TenantEntity tenant1 = new TenantEntity();
        tenant1.setId("tenant-1");
        tenant1.setName("Tenant 1");

        TenantEntity tenant2 = new TenantEntity();
        tenant2.setId("tenant-2");
        tenant2.setName("Tenant 2");

        when(tenantRepository.findAll()).thenReturn(List.of(tenant1, tenant2));

        // When
        List<TenantEntity> result = tenantService.getAllTenants();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void createTenant_ShouldSaveTenant() {
        // Given
        TenantEntity tenant = new TenantEntity();
        tenant.setId(TEST_TENANT_ID);
        tenant.setName("New Tenant");

        when(tenantRepository.save(any())).thenReturn(tenant);

        // When
        TenantEntity result = tenantService.createTenant(tenant);

        // Then
        assertNotNull(result);
        assertEquals(TEST_TENANT_ID, result.getId());
        verify(tenantRepository, times(1)).save(tenant);
    }

    @Test
    void updateTenant_ShouldSaveUpdatedTenant() {
        // Given
        TenantEntity tenant = new TenantEntity();
        tenant.setId(TEST_TENANT_ID);
        tenant.setName("Updated Tenant");

        when(tenantRepository.save(any())).thenReturn(tenant);

        // When
        TenantEntity result = tenantService.updateTenant(tenant);

        // Then
        assertNotNull(result);
        assertEquals("Updated Tenant", result.getName());
        verify(tenantRepository, times(1)).save(tenant);
    }

    @Test
    void deleteTenant_ShouldDeleteTenant() {
        // Given
        doNothing().when(tenantRepository).deleteById(TEST_TENANT_ID);

        // When
        tenantService.deleteTenant(TEST_TENANT_ID);

        // Then
        verify(tenantRepository, times(1)).deleteById(TEST_TENANT_ID);
    }
}