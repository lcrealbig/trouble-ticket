package com.example.ticketer.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import com.example.ticketer.service.TenantService;
import com.example.ticketer.persistence.entity.TenantEntity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
@Order(2)
public class DatabaseInitializer {

    private final TenantService tenantService;

    public DatabaseInitializer(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Bean
    @Profile({"debug", "local"})
    public CommandLineRunner initDatabase() {
        return args -> {
            try {
                // Determine the database host based on environment
                String dbHost = System.getenv("SPRING_DATASOURCE_URL") != null && 
                               System.getenv("SPRING_DATASOURCE_URL").contains("postgres") ? 
                               "postgres" : "localhost";
                
                // Connect to default postgres database to create our target database
                try (Connection connection = DriverManager.getConnection(
                        "jdbc:postgresql://" + dbHost + ":5432/postgres", "postgres", "postgres")) {
                    
                    // Check if database exists first
                    boolean databaseExists = false;
                    try (Statement statement = connection.createStatement();
                         var resultSet = statement.executeQuery(
                                 "SELECT 1 FROM pg_database WHERE datname = 'trouble_ticket'")) {
                        databaseExists = resultSet.next();
                    }
                    
                    // Create database if it doesn't exist
                    if (!databaseExists) {
                        try (Statement statement = connection.createStatement()) {
                            statement.execute("CREATE DATABASE trouble_ticket");
                            System.out.println("Created database: trouble_ticket");
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println("Database initialization error: " + e.getMessage());
                // Continue even if database creation fails - it might already exist
            }
        };
    }

    @Bean
    @Profile({"debug", "local"})
    @Order(3)
    public CommandLineRunner initTenants() {
        return args -> {
            try {
                // Wait a bit to ensure Flyway migrations have completed
                Thread.sleep(5000);
                
                // Create tenants matching the user emails from SecurityConfig
                createTenantIfNotExists("user@example.com", "User Tenant");
                createTenantIfNotExists("admin@example.com", "Admin Tenant");
                
                System.out.println("Tenant initialization completed");
            } catch (Exception e) {
                System.out.println("Tenant initialization error: " + e.getMessage());
            }
        };
    }

    private void createTenantIfNotExists(String tenantName, String displayName) {
        try {
            if (tenantService.findTenantName(tenantName).isEmpty()) {
                TenantEntity tenant = new TenantEntity();
                tenant.setId(tenantName);
                tenant.setName(displayName);
                tenantService.createTenant(tenant);
                System.out.println("Created tenant: " + tenantName + " (" + displayName + ")");
            } else {
                System.out.println("Tenant already exists: " + tenantName);
            }
        } catch (Exception e) {
            System.out.println("Error creating tenant " + tenantName + ": " + e.getMessage());
        }
    }
}