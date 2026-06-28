CREATE SEQUENCE tenants_id_seq START 1 INCREMENT 1;

CREATE TABLE tenant (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE SEQUENCE trouble_ticket_id_seq START 1 INCREMENT 1;

CREATE TABLE trouble_ticket (
    id BIGINT PRIMARY KEY DEFAULT nextval('trouble_ticket_id_seq'),
    external_id UUID NOT NULL UNIQUE,
    service_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    tenant_id VARCHAR(50) NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE SEQUENCE note_id_seq START 1 INCREMENT 1;

CREATE TABLE note (
    id BIGINT PRIMARY KEY DEFAULT nextval('note_id_seq'),
    trouble_ticket_id BIGINT NOT NULL REFERENCES trouble_ticket(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_trouble_tickets_tenant_id ON trouble_ticket(tenant_id);
CREATE INDEX idx_trouble_tickets_external_id ON trouble_ticket(external_id);
CREATE INDEX idx_notes_trouble_ticket_id ON note(trouble_ticket_id);