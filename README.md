# Trouble Ticket API - Docker Development Environment

## Overview

This project provides a multi-tenant trouble ticket system with Spring Boot, PostgreSQL, and pgAdmin for local development.

## Prerequisites

- Docker Desktop or Docker Engine running on your machine
- Java 21 (for local development)
- Maven (for local development)

## Quick Start

### 1. Start the full environment with Docker Compose

```bash
docker-compose -f compose.yaml up --build
```

This will start:
- PostgreSQL 15 with persistent volume
- Spring Boot application
- pgAdmin 4 (available at http://localhost:5050)

### 2. Access the services

- **API**: http://localhost:8080/api/v1/actuator/health
- **pgAdmin**: http://localhost:5050
  - Email: admin@example.com
  - Password: admin
- **PostgreSQL**: localhost:5432
  - Database: trouble_ticket
  - User: postgres
  - Password: postgres


## Local Development with IDE

### 1. Start only the infrastructure services

```bash
docker-compose -f compose.yaml up postgres pgadmin
```

### 2. Configure your IDE

Run the Spring Boot application with the `local` profile:

```bash
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

Or configure your IDE run configuration with:
- Active profile: `local`
- Environment variables - all variables are open on purpose.

## Database Setup

### Connect to PostgreSQL via pgAdmin

1. Open pgAdmin at http://localhost:5050
2. Login with:
   - Email: admin@example.com
   - Password: admin
3. Right-click "Servers" > "Register" > "Server"
4. In the "General" tab, enter:
   - Name: Trouble Ticket DB
5. In the "Connection" tab, enter:
   - Host name/address: postgres
   - Port: 5432
   - Maintenance database: postgres
   - Username: postgres
   - Password: postgres

The application uses JWT-based tenant isolation:

1. Tenant ID is extracted from JWT token in `JwtAuthenticationFilter`
2. Tenant ID is stored in `TenantContext` (request-scoped bean)
3. All database operations are filtered by tenant ID

