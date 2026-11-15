-- =============================================================================
-- CIVICBUILD MUNICIPAL BUILDING PERMIT AUTOMATION PORTAL
-- Complete Database Schema (PostgreSQL 16 Compliant)
-- =============================================================================

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    department_code VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS properties (
    id BIGSERIAL PRIMARY KEY,
    address TEXT NOT NULL,
    survey_number VARCHAR(50) NOT NULL,
    ward_number VARCHAR(20) NOT NULL,
    zone VARCHAR(50) NOT NULL,
    plot_area NUMERIC(12,2) NOT NULL,
    ownership_type VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS applications (
    id BIGSERIAL PRIMARY KEY,
    application_number VARCHAR(50) UNIQUE NOT NULL,
    applicant_id BIGINT NOT NULL REFERENCES users(id),
    property_id BIGINT NOT NULL REFERENCES properties(id),
    status VARCHAR(50) NOT NULL,
    building_type VARCHAR(50) NOT NULL,
    construction_purpose VARCHAR(100) NOT NULL,
    number_of_floors INTEGER NOT NULL,
    built_up_area NUMERIC(12,2) NOT NULL,
    building_height NUMERIC(8,2) NOT NULL,
    estimated_cost NUMERIC(14,2) NOT NULL,
    parking_available BOOLEAN NOT NULL DEFAULT TRUE,
    process_instance_key VARCHAR(100),
    submission_date TIMESTAMP WITHOUT TIME ZONE,
    approved_date TIMESTAMP WITHOUT TIME ZONE,
    rejected_date TIMESTAMP WITHOUT TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    uploaded_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    contact_email VARCHAR(100) NOT NULL,
    sla_days INTEGER NOT NULL DEFAULT 7
);

CREATE TABLE IF NOT EXISTS department_reviews (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    department_code VARCHAR(50) NOT NULL,
    officer_id BIGINT REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    remarks TEXT,
    due_date TIMESTAMP WITHOUT TIME ZONE,
    reviewed_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS revision_requests (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    department_code VARCHAR(50) NOT NULL,
    officer_remarks TEXT NOT NULL,
    cycle_number INTEGER NOT NULL DEFAULT 1,
    requested_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'REQUESTED'
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(100) UNIQUE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_date TIMESTAMP WITHOUT TIME ZONE,
    gateway_response TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS permits (
    id BIGSERIAL PRIMARY KEY,
    permit_number VARCHAR(50) UNIQUE NOT NULL,
    application_id BIGINT UNIQUE NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    issue_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    qr_code_data TEXT NOT NULL,
    pdf_path VARCHAR(500) NOT NULL,
    issued_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_histories (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    stage VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comments TEXT,
    process_instance_key VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_email VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_status BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);

-- =============================================================================
-- INDEXES FOR PERFORMANCE & LOOKUPS
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_applications_number ON applications(application_number);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_applications_applicant ON applications(applicant_id);
CREATE INDEX IF NOT EXISTS idx_reviews_app_dept ON department_reviews(application_id, department_code);
CREATE INDEX IF NOT EXISTS idx_reviews_status ON department_reviews(status);
CREATE INDEX IF NOT EXISTS idx_payments_app ON payments(application_id);
CREATE INDEX IF NOT EXISTS idx_permits_number ON permits(permit_number);
CREATE INDEX IF NOT EXISTS idx_history_app ON workflow_histories(application_id);
