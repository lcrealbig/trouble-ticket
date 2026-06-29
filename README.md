# Trouble Ticket API - Implementation Documentation

TL;DR One may do not like a long reading so heres a command that runs the app via compose.:
### Quick Start with Docker

```bash
docker-compose -f compose.yaml up --build
```

This starts:
- PostgreSQL 15 with persistent volume
- Spring Boot application on port 8080
- pgAdmin 4 on port 5050 for database management
- 
## Authentication
The API uses JWT Bearer tokens for authentication:

## Example Usage
```bash
curl -X POST "http://localhost:8080/api/v1/auth/token" \
  -H "Authorization: Basic dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZA==" \
  -H "Content-Type: application/json"
```

## Overview

This project implements a multi-tenant Trouble Ticket API based on the OpenAPI specification.

### Key Design Decisions Addressed

This documentation specifically addresses the following implementation choices:

1. **Asynchronous Communication**:
2. **ID Field Removal**: Internal database IDs are not exposed in API responses
3. **Pagination Implementation**: Added pagination to collection endpoints for performance

These decisions are explained in detail throughout this documentation.

### Key Architectural Decisions

#### 1. Synchronous Implementation 

The current implementation follows a **KISS (Keep It Simple, Silly)** approach with synchronous REST endpoints, but the design is prepared for future asynchronous enhancements:

- **Synchronous REST API**: Current implementation uses standard request-response pattern
- **Idempotency Support**: Uses external IDs for safe retries (preparation for async processing)
- **Status Workflow**: Supports status transitions that could be processed asynchronously
- **Contract Compliance**: Fully implements the OpenAPI specification requirements

**Current Architecture**:
- RESTful synchronous endpoints
- PostgreSQL for persistent storage
- In-memory processing of requests
- Immediate responses to clients

**Future Async-Ready Design**:
The system could evolve to:
- Consume tickets from Kafka topics
- Cache frequently accessed lists in Redis

**Rationale for Current Approach**:
- Simpler to implement and maintain
- Easier to debug and monitor
- Meets current requirements with minimal complexity
- Can scale to async patterns when needed

#### 2. ID Field Removal from Response

**Decision**: The internal database ID (`id`) has been removed from API responses, exposing only the `externalId`.

**Rationale**:
- **Security**: Prevents exposure of internal database keys that could be used for enumeration attacks
- **Contract Compliance**: The OpenAPI specification allows for this flexibility in implementation
- **Client-Friendly**: Clients work with their own `externalId` values, which are more meaningful in their context
- **Idempotency**: The `(tenantId, externalId)` tuple provides the idempotency key for operations
- **Best Practice**: Follows the principle of least privilege by not exposing internal implementation details

**Implementation**:
- Database still uses auto-generated IDs internally for relationships and indexing
- All external APIs use `externalId` as the primary identifier
- Mapping between internal ID and external ID is handled transparently by the service layer

#### 3. Pagination Implementation

**Decision**: Added pagination to the `GET /troubleTicket` endpoint despite the OpenAPI spec not requiring it in v1.

**Rationale**:
- **Performance**: Prevents loading large datasets that could impact API performance
- **Scalability**: Supports future growth as ticket volumes increase
- **User Experience**: Provides predictable response times and memory usage
- **Best Practice**: Follows REST API design guidelines for collection resources

**Implementation Details**:
- Uses Spring Data's `Page` and `PageRequest` for consistent pagination
- Default page size: 20 items
- Configurable via query parameters: `?page=0&size=50`
- Returns pagination metadata in response headers and body
- Maintains backward compatibility (defaults work without parameters)

## API Design Principles

### Contract-First Development

- **OpenAPI Specification**: The API is fully defined by the OpenAPI 3.1 specification
- **Strict Validation**: Request/response payloads are validated against the schema
- **Versioning**: Uses `/api/v1` path versioning for backward compatibility
- **Idempotency**: POST operations are idempotent using `(tenantId, externalId)` tuple

### Security

- **JWT Authentication**: Bearer token authentication with tenant scope extraction
- **Tenant Isolation**: All operations are scoped to the authenticated tenant
- **Role-Based Access**: Different roles can have different access levels
- **Input Validation**: Comprehensive validation of all inputs

### Data Model
- Is available at src/main/resources/database/Ticketer-Database-Model.puml

## Technical Stack

- **Framework**: Spring Boot 4.x with Java 21
- **Database**: PostgreSQL 15 with JPA/Hibernate
- **Security**: Spring Security with JWT
- **API Docs**: OpenAPI 3.1 with Swagger UI
- **Metrics**: Micrometer with Prometheus
- **Containerization**: Docker with Docker Compose
- **Build**: Maven

## Setup and Running

### Prerequisites

- Docker Desktop or Docker Engine
- Java 21 (for local development)
- Maven 3.8+ (for local development)

### Accessing Services

- **API**: `http://localhost:8080/api/v1`
- **Health Check**: `http://localhost:8080/api/v1/actuator/health`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **pgAdmin**: `http://localhost:5050` (admin@example.com/admin)
- **PostgreSQL**: `localhost:5432` (postgres/postgres)

## API Endpoints
### Trouble Ticket Operations
Available in OpenApi.yaml

### Scaling

- **Stateless Design**: The application is stateless and can be horizontally scaled
- **Database**: PostgreSQL can be scaled with read replicas for reporting
- **Caching**: Consider adding Redis for frequently accessed tickets
- **Async Processing**: Status updates can be processed via message queues

### Monitoring and Metrics

The application includes comprehensive monitoring capabilities using Spring Boot Actuator and Micrometer.

### Built-in Metrics

**Automatic Metrics:**
- HTTP request/response metrics (count, duration, status codes)
- JVM metrics (memory usage, thread count, garbage collection)
- Database connection pool metrics
- System metrics (CPU, disk I/O)

**Custom Business Metrics:**
- `trouble.ticket.controller` - Execution time for all trouble ticket endpoints
- Method-level timing via `@Timed` annotations

### Accessing Metrics Locally

**Actuator Endpoints:**
```bash
# List all available metrics
curl http://localhost:8080/api/v1/actuator/metrics

# View HTTP request metrics
curl http://localhost:8080/api/v1/actuator/metrics/http.server.requests

# Get Prometheus format (for monitoring systems)
curl http://localhost:8080/api/v1/actuator/prometheus
```

**Browser Access:**
- `http://localhost:8080/api/v1/actuator/metrics` - All metrics
- `http://localhost:8080/api/v1/actuator/prometheus` - Prometheus format
- `http://localhost:8080/api/v1/actuator/health` - Health status

### Code Quality

- Follow Spring Boot conventions
- Use Lombok for boilerplate reduction
- Write comprehensive unit  tests
- Maintain OpenAPI specification in sync with implementation
- Document all public API changes
- Implement proper tenant isolation in all data access
- Use DTOs for API boundaries, never expose entities directly

## License

This project is licensed under the MIT License - see the LICENSE file for details.
---