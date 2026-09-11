-- Hospital Management System - MySQL production schema
-- Run this against a MySQL 8+ server, then follow docs/DATABASE_GUIDE.md
-- to point DatabaseConnection at it instead of the bundled SQLite file.

CREATE DATABASE IF NOT EXISTS hospital_management
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hospital_management;

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          ENUM('ADMIN','DOCTOR','RECEPTIONIST') NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(120) NOT NULL,
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    avatar_path   VARCHAR(255),
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login    DATETIME     NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS doctors (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    doctor_code        VARCHAR(20)  NOT NULL UNIQUE,
    full_name          VARCHAR(120) NOT NULL,
    department         VARCHAR(80)  NOT NULL,
    specialization     VARCHAR(120) NOT NULL,
    phone              VARCHAR(20)  NOT NULL,
    email              VARCHAR(120) NOT NULL,
    working_schedule   VARCHAR(200),
    consultation_fee   DECIMAL(10,2) NOT NULL DEFAULT 0,
    user_id            INT NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_doctors_department (department)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rooms (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    room_code           VARCHAR(20) NOT NULL UNIQUE,
    room_type           ENUM('GENERAL_WARD','PRIVATE','ICU','PSYCHIATRIC_UNIT','MATERNITY','PEDIATRIC_WARD') NOT NULL,
    capacity            INT NOT NULL DEFAULT 1,
    floor               VARCHAR(40),
    notes               VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS patients (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    patient_code        VARCHAR(20)  NOT NULL UNIQUE,
    full_name           VARCHAR(120) NOT NULL,
    gender              VARCHAR(10)  NOT NULL,
    age                 INT NOT NULL,
    date_of_birth       DATE,
    phone               VARCHAR(20)  NOT NULL,
    email               VARCHAR(120),
    address             VARCHAR(255),
    blood_group         VARCHAR(5),
    emergency_contact   VARCHAR(20),
    room_id             INT NULL,
    registered_on       DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_patient_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL,
    INDEX idx_patients_name (full_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    appointment_code    VARCHAR(20) NOT NULL UNIQUE,
    patient_id          INT NOT NULL,
    doctor_id           INT NOT NULL,
    department          VARCHAR(80) NOT NULL,
    appointment_date    DATE NOT NULL,
    appointment_time    TIME NOT NULL,
    status              ENUM('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    reason              VARCHAR(255),
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_appt_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors(id)  ON DELETE CASCADE,
    UNIQUE KEY uq_doctor_slot (doctor_id, appointment_date, appointment_time),
    INDEX idx_appointments_date (appointment_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS medical_records (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id      INT NULL,
    patient_id          INT NOT NULL,
    doctor_id           INT NOT NULL,
    diagnosis           TEXT,
    symptoms            TEXT,
    prescription        TEXT,
    lab_result          TEXT,
    doctor_notes        TEXT,
    record_date         DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_record_appt    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    CONSTRAINT fk_record_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_record_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors(id)  ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS invoices (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    invoice_code          VARCHAR(20) NOT NULL UNIQUE,
    patient_id            INT NOT NULL,
    appointment_id        INT NULL,
    consultation_fee      DECIMAL(10,2) NOT NULL DEFAULT 0,
    medicine_fee          DECIMAL(10,2) NOT NULL DEFAULT 0,
    lab_fee               DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount              DECIMAL(10,2) NOT NULL DEFAULT 0,
    tax                   DECIMAL(10,2) NOT NULL DEFAULT 0,
    total                 DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_status        ENUM('PAID','UNPAID','PARTIAL') NOT NULL DEFAULT 'UNPAID',
    issued_on             DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_invoice_patient     FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_log (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NULL,
    username      VARCHAR(50) NOT NULL,
    action        VARCHAR(80) NOT NULL,
    details       VARCHAR(255),
    occurred_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_log_time (occurred_at)
) ENGINE=InnoDB;

-- Default admin: username 'admin' / password 'admin123'
-- (this is the same jBCrypt hash used by the bundled SQLite schema, verified working)
INSERT INTO users (username, password_hash, role, full_name, email, active)
VALUES ('admin', '$2a$12$gs0G4oZCfJYNA6KCwe/kWuECgv5stlvFr6WLnU16P8As4SQt6dS9u', 'ADMIN', 'System Administrator', 'admin@hospital.local', 1)
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO doctors (doctor_code, full_name, department, specialization, phone, email, working_schedule, consultation_fee)
VALUES ('DOC-003', 'Lena Vireak', 'Psychiatry', 'Adult & Adolescent Psychiatry', '012-345-680', 'lena.vireak@hospital.local', 'Tue-Sat 10:00-18:00', 55.00)
ON DUPLICATE KEY UPDATE doctor_code = doctor_code;

INSERT INTO rooms (room_code, room_type, capacity, floor, notes)
VALUES ('PSY-301', 'PSYCHIATRIC_UNIT', 2, '3rd Floor', 'Secure psychiatric unit')
ON DUPLICATE KEY UPDATE room_code = room_code;
