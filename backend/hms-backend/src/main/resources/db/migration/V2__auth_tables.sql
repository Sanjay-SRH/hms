-- Users, roles and permissions (many-to-many)

CREATE TABLE app_user (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username              VARCHAR(50)  NOT NULL UNIQUE,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password_hash         VARCHAR(100) NOT NULL,
    full_name             VARCHAR(150) NOT NULL,
    active                BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          TIMESTAMPTZ,
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version               BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE app_role (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE permission (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES app_role(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permission (
    role_id       UUID NOT NULL REFERENCES app_role(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permission(id),
    PRIMARY KEY (role_id, permission_id)
);

-- Seed roles
INSERT INTO app_role (name, description) VALUES
    ('ADMIN',      'System administrator'),
    ('REGISTRAR',  'Front-desk patient registration'),
    ('DOCTOR',     'Doctor'),
    ('NURSE',      'Nurse'),
    ('PHARMACIST', 'Pharmacy staff'),
    ('BILLING',    'Billing desk'),
    ('MRD',        'Medical records department');

-- Seed permissions
INSERT INTO permission (code, description) VALUES
    ('USER_MANAGE',                'Create, edit and deactivate users'),
    ('AUDIT_READ',                 'View the audit log'),
    ('PATIENT_READ',               'View patient records'),
    ('PATIENT_CREATE',             'Register new patients'),
    ('PATIENT_UPDATE_DEMOGRAPHICS','Edit phone, address and other demographics'),
    ('PATIENT_UPDATE_IDENTITY',    'Edit name, date of birth or gender'),
    ('PATIENT_MERGE',              'Merge duplicate patient records'),
    ('CLINICAL_READ',              'View clinical data'),
    ('CLINICAL_WRITE',             'Write clinical data');

-- Map roles to permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM app_role r
JOIN permission p ON (r.name, p.code) IN (VALUES
    ('ADMIN',      'USER_MANAGE'),
    ('ADMIN',      'AUDIT_READ'),
    ('REGISTRAR',  'PATIENT_READ'),
    ('REGISTRAR',  'PATIENT_CREATE'),
    ('REGISTRAR',  'PATIENT_UPDATE_DEMOGRAPHICS'),
    ('DOCTOR',     'PATIENT_READ'),
    ('DOCTOR',     'CLINICAL_READ'),
    ('DOCTOR',     'CLINICAL_WRITE'),
    ('NURSE',      'PATIENT_READ'),
    ('NURSE',      'CLINICAL_READ'),
    ('NURSE',      'CLINICAL_WRITE'),
    ('PHARMACIST', 'PATIENT_READ'),
    ('PHARMACIST', 'CLINICAL_READ'),
    ('BILLING',    'PATIENT_READ'),
    ('MRD',        'PATIENT_READ'),
    ('MRD',        'PATIENT_UPDATE_IDENTITY'),
    ('MRD',        'PATIENT_MERGE'),
    ('MRD',        'AUDIT_READ')
);