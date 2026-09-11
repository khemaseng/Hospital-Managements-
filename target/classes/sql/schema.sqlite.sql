-- Hospital Management System - SQLite schema
-- Loaded automatically by DatabaseConnection.initializeDatabase() on startup.
-- All statements are idempotent (IF NOT EXISTS) so this is safe to re-run.
-- Databases created by an older version of this app get the newer columns
-- added by DatabaseConnection.runMigrations() instead (additive, non-destructive).

CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role          TEXT NOT NULL CHECK (role IN ('ADMIN','DOCTOR','RECEPTIONIST')),
    full_name     TEXT NOT NULL,
    email         TEXT NOT NULL,
    active        INTEGER NOT NULL DEFAULT 1,
    avatar_path   TEXT,
    created_at    TEXT NOT NULL DEFAULT (datetime('now')),
    last_login    TEXT
);

CREATE TABLE IF NOT EXISTS doctors (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    doctor_code        TEXT NOT NULL UNIQUE,
    full_name          TEXT NOT NULL,
    department         TEXT NOT NULL,
    specialization     TEXT NOT NULL,
    phone              TEXT NOT NULL,
    email              TEXT NOT NULL,
    working_schedule   TEXT,
    consultation_fee   REAL NOT NULL DEFAULT 0,
    user_id            INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at         TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS rooms (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    room_code           TEXT NOT NULL UNIQUE,
    room_type           TEXT NOT NULL CHECK (room_type IN
                         ('GENERAL_WARD','PRIVATE','ICU','PSYCHIATRIC_UNIT','MATERNITY','PEDIATRIC_WARD')),
    capacity            INTEGER NOT NULL DEFAULT 1,
    floor                TEXT,
    notes                TEXT
);

CREATE TABLE IF NOT EXISTS patients (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_code        TEXT NOT NULL UNIQUE,
    full_name           TEXT NOT NULL,
    gender              TEXT NOT NULL,
    age                 INTEGER NOT NULL,
    date_of_birth       TEXT,
    phone               TEXT NOT NULL,
    email               TEXT,
    address             TEXT,
    blood_group         TEXT,
    emergency_contact   TEXT,
    room_id             INTEGER REFERENCES rooms(id) ON DELETE SET NULL,
    registered_on       TEXT NOT NULL DEFAULT (date('now'))
);

CREATE TABLE IF NOT EXISTS appointments (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    appointment_code    TEXT NOT NULL UNIQUE,
    patient_id          INTEGER NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id           INTEGER NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    department          TEXT NOT NULL,
    appointment_date    TEXT NOT NULL,
    appointment_time    TEXT NOT NULL,
    status              TEXT NOT NULL DEFAULT 'SCHEDULED'
                         CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW')),
    reason              TEXT,
    created_at          TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE (doctor_id, appointment_date, appointment_time)
);

CREATE TABLE IF NOT EXISTS medical_records (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    appointment_id      INTEGER REFERENCES appointments(id) ON DELETE SET NULL,
    patient_id          INTEGER NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id           INTEGER NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    diagnosis           TEXT,
    symptoms            TEXT,
    prescription         TEXT,
    lab_result           TEXT,
    doctor_notes          TEXT,
    record_date          TEXT NOT NULL DEFAULT (date('now'))
);

CREATE TABLE IF NOT EXISTS invoices (
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_code          TEXT NOT NULL UNIQUE,
    patient_id            INTEGER NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    appointment_id         INTEGER REFERENCES appointments(id) ON DELETE SET NULL,
    consultation_fee       REAL NOT NULL DEFAULT 0,
    medicine_fee           REAL NOT NULL DEFAULT 0,
    lab_fee                REAL NOT NULL DEFAULT 0,
    discount               REAL NOT NULL DEFAULT 0,
    tax                    REAL NOT NULL DEFAULT 0,
    total                  REAL NOT NULL DEFAULT 0,
    payment_status          TEXT NOT NULL DEFAULT 'UNPAID' CHECK (payment_status IN ('PAID','UNPAID','PARTIAL')),
    issued_on              TEXT NOT NULL DEFAULT (date('now'))
);

CREATE TABLE IF NOT EXISTS audit_log (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER REFERENCES users(id) ON DELETE SET NULL,
    username      TEXT NOT NULL,
    action        TEXT NOT NULL,
    details       TEXT,
    occurred_at   TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_patients_name ON patients(full_name);
CREATE INDEX IF NOT EXISTS idx_doctors_department ON doctors(department);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor ON appointments(doctor_id, appointment_date);
CREATE INDEX IF NOT EXISTS idx_audit_log_time ON audit_log(occurred_at);

-- Seed data: default admin account (username: admin / password: admin123)
-- Hash below is a BCrypt hash of "admin123" - change it after first login.
INSERT OR IGNORE INTO users (id, username, password_hash, role, full_name, email, active)
VALUES (1, 'admin', '$2a$12$gs0G4oZCfJYNA6KCwe/kWuECgv5stlvFr6WLnU16P8As4SQt6dS9u', 'ADMIN', 'System Administrator', 'admin@hospital.local', 1);

INSERT OR IGNORE INTO doctors (id, doctor_code, full_name, department, specialization, phone, email, working_schedule, consultation_fee)
VALUES
 (1, 'DOC-001', 'Sarah Chen', 'Cardiology', 'Interventional Cardiology', '012-345-678', 'sarah.chen@hospital.local', 'Mon-Fri 08:00-16:00', 45.00),
 (2, 'DOC-002', 'Michael Rous', 'Pediatrics', 'General Pediatrics', '012-345-679', 'michael.rous@hospital.local', 'Mon-Sat 09:00-17:00', 30.00),
 (3, 'DOC-003', 'Lena Vireak', 'Psychiatry', 'Adult & Adolescent Psychiatry', '012-345-680', 'lena.vireak@hospital.local', 'Tue-Sat 10:00-18:00', 55.00);

INSERT OR IGNORE INTO rooms (id, room_code, room_type, capacity, floor, notes)
VALUES
 (1, 'GW-101', 'GENERAL_WARD', 4, '1st Floor', 'Shared ward, near nurse station'),
 (2, 'PR-201', 'PRIVATE', 1, '2nd Floor', 'Private room with attached bathroom'),
 (3, 'ICU-01', 'ICU', 1, '3rd Floor', 'Intensive care, continuous monitoring'),
 (4, 'PSY-301', 'PSYCHIATRIC_UNIT', 2, '3rd Floor', 'Secure psychiatric unit');

INSERT OR IGNORE INTO patients (id, patient_code, full_name, gender, age, date_of_birth, phone, email, address, blood_group, emergency_contact)
VALUES
 (1, 'PAT-0001', 'Sok Dara', 'Male', 34, '1992-03-14', '098-111-222', 'sok.dara@example.com', 'Phnom Penh', 'O+', '098-999-000');

-- ============================================================
-- Extended demo/seed data (33 additional doctors, 34 additional
-- patients, ~119 appointments, 50 medical records, ~58 invoices,
-- 6 room assignments) so every screen has real content to show
-- instead of empty tables. Safe to re-run (INSERT OR IGNORE).
-- Every text value here is verified free of semicolons and raw
-- single quotes, since DatabaseConnection strips full comment
-- lines but still splits statements on literal semicolons.
-- ============================================================

INSERT OR IGNORE INTO doctors (id, doctor_code, full_name, department, specialization, phone, email, working_schedule, consultation_fee)
VALUES
 (4, 'DOC-004', 'James Mitchell', 'General Medicine', 'Primary Care', '012-428-242', 'james.mitchell@hospital.local', 'Mon-Fri 12:00-20:00', 42.88),
 (5, 'DOC-005', 'Kenneth Ramirez', 'Cardiology', 'Interventional Cardiology', '012-295-323', 'kenneth.ramirez@hospital.local', 'Mon-Sat 09:00-17:00', 27.07),
 (6, 'DOC-006', 'Joshua Gonzalez', 'Pediatrics', 'Pediatric Pulmonology', '012-918-658', 'joshua.gonzalez@hospital.local', 'Mon-Thu 07:00-15:00', 71.54),
 (7, 'DOC-007', 'Emily Moore', 'Orthopedics', 'Sports Medicine', '012-206-877', 'emily.moore@hospital.local', 'Mon-Sat 09:00-17:00', 77.61),
 (8, 'DOC-008', 'Nancy Moore', 'Neurology', 'Movement Disorders', '012-981-444', 'nancy.moore@hospital.local', 'Mon-Fri 08:00-16:00', 35.11),
 (9, 'DOC-009', 'Linda Thompson', 'Dermatology', 'Medical Dermatology', '012-818-370', 'linda.thompson@hospital.local', 'Mon-Fri 08:00-16:00', 80.09),
 (10, 'DOC-010', 'Kimberly Davis', 'Radiology', 'Neuroradiology', '012-587-180', 'kimberly.davis@hospital.local', 'Wed-Sun 11:00-19:00', 88.25),
 (11, 'DOC-011', 'Emily Gonzalez', 'Surgery', 'Vascular Surgery', '012-246-777', 'emily.gonzalez@hospital.local', 'Mon-Sat 09:00-17:00', 70.8),
 (12, 'DOC-012', 'Ryan Anderson', 'Psychiatry', 'Child & Adolescent Psychiatry', '012-589-384', 'ryan.anderson@hospital.local', 'Mon-Thu 07:00-15:00', 81.32),
 (13, 'DOC-013', 'Barbara White', 'General Medicine', 'Primary Care', '012-886-373', 'barbara.white@hospital.local', 'Mon-Fri 12:00-20:00', 48.09),
 (14, 'DOC-014', 'Kevin Green', 'Cardiology', 'Heart Failure & Transplant', '012-946-350', 'kevin.green@hospital.local', 'Mon-Sat 09:00-17:00', 36.12),
 (15, 'DOC-015', 'Sarah Green', 'Pediatrics', 'Neonatology', '012-424-801', 'sarah.green@hospital.local', 'Tue-Sat 10:00-18:00', 69.73),
 (16, 'DOC-016', 'Jeffrey Williams', 'Orthopedics', 'Sports Medicine', '012-610-374', 'jeffrey.williams@hospital.local', 'Mon-Fri 08:00-16:00', 77.33),
 (17, 'DOC-017', 'Susan Adams', 'Neurology', 'Stroke & Vascular Neurology', '012-858-569', 'susan.adams@hospital.local', 'Mon-Sat 09:00-17:00', 57.45),
 (18, 'DOC-018', 'Joseph Mitchell', 'Dermatology', 'Cosmetic Dermatology', '012-469-864', 'joseph.mitchell@hospital.local', 'Wed-Sun 11:00-19:00', 61.49),
 (19, 'DOC-019', 'Lisa Anderson', 'Radiology', 'Interventional Radiology', '012-341-621', 'lisa.anderson@hospital.local', 'Mon-Thu 07:00-15:00', 89.83),
 (20, 'DOC-020', 'Ryan Davis', 'Surgery', 'General Surgery', '012-363-911', 'ryan.davis@hospital.local', 'Mon-Fri 12:00-20:00', 34.93),
 (21, 'DOC-021', 'Anthony Harris', 'Psychiatry', 'Child & Adolescent Psychiatry', '012-679-641', 'anthony.harris@hospital.local', 'Tue-Sat 10:00-18:00', 63.73),
 (22, 'DOC-022', 'George Campbell', 'General Medicine', 'Primary Care', '012-749-868', 'george.campbell@hospital.local', 'Tue-Sat 10:00-18:00', 32.45),
 (23, 'DOC-023', 'Linda Jackson', 'Cardiology', 'Heart Failure & Transplant', '012-664-103', 'linda.jackson@hospital.local', 'Mon-Fri 12:00-20:00', 53.26),
 (24, 'DOC-024', 'Dorothy Carter', 'Pediatrics', 'Pediatric Pulmonology', '012-308-991', 'dorothy.carter@hospital.local', 'Mon-Fri 12:00-20:00', 36.61),
 (25, 'DOC-025', 'David White', 'Orthopedics', 'Joint Replacement', '012-752-897', 'david.white@hospital.local', 'Wed-Sun 11:00-19:00', 74.56),
 (26, 'DOC-026', 'Ashley Johnson', 'Neurology', 'Stroke & Vascular Neurology', '012-571-999', 'ashley.johnson@hospital.local', 'Tue-Sat 10:00-18:00', 32.27),
 (27, 'DOC-027', 'Joseph Torres', 'Dermatology', 'Medical Dermatology', '012-287-849', 'joseph.torres@hospital.local', 'Mon-Thu 07:00-15:00', 86.55),
 (28, 'DOC-028', 'David Nelson', 'Radiology', 'Diagnostic Radiology', '012-762-269', 'david.nelson@hospital.local', 'Tue-Sat 10:00-18:00', 55.89),
 (29, 'DOC-029', 'Susan Wright', 'Surgery', 'Colorectal Surgery', '012-906-305', 'susan.wright@hospital.local', 'Mon-Fri 12:00-20:00', 74.09),
 (30, 'DOC-030', 'Carol Adams', 'Psychiatry', 'Child & Adolescent Psychiatry', '012-729-562', 'carol.adams@hospital.local', 'Mon-Fri 08:00-16:00', 49.27),
 (31, 'DOC-031', 'Robert Perez', 'General Medicine', 'Internal Medicine', '012-767-335', 'robert.perez@hospital.local', 'Wed-Sun 11:00-19:00', 26.37),
 (32, 'DOC-032', 'Robert Rivera', 'Cardiology', 'Interventional Cardiology', '012-434-169', 'robert.rivera@hospital.local', 'Mon-Fri 08:00-16:00', 66.02),
 (33, 'DOC-033', 'Andrew Thomas', 'Pediatrics', 'Neonatology', '012-697-319', 'andrew.thomas@hospital.local', 'Wed-Sun 11:00-19:00', 43.1),
 (34, 'DOC-034', 'Jessica Walker', 'Orthopedics', 'Sports Medicine', '012-394-196', 'jessica.walker@hospital.local', 'Mon-Fri 08:00-16:00', 77.49),
 (35, 'DOC-035', 'Lisa Ramirez', 'Neurology', 'Movement Disorders', '012-946-155', 'lisa.ramirez@hospital.local', 'Mon-Fri 12:00-20:00', 51.72),
 (36, 'DOC-036', 'Michael Sanchez', 'Dermatology', 'Mohs Surgery', '012-311-354', 'michael.sanchez@hospital.local', 'Mon-Sat 09:00-17:00', 72.33);

INSERT OR IGNORE INTO patients (id, patient_code, full_name, gender, age, date_of_birth, phone, email, address, blood_group, emergency_contact, registered_on)
VALUES
 (2, 'PAT-0002', 'Joshua Lewis', 'Male', 21, '2005-01-22', '012-385-573', 'joshua.lewis32@example.com', 'Austin, TX', 'O-', '098-151-767', '2025-11-18'),
 (3, 'PAT-0003', 'Robert Carter', 'Male', 34, '1992-06-05', '070-597-592', 'robert.carter28@example.com', 'San Diego, CA', 'A+', '012-488-102', '2026-02-03'),
 (4, 'PAT-0004', 'Cynthia Robinson', 'Female', 40, '1986-01-27', '088-777-835', 'cynthia.robinson63@example.com', 'Portland, OR', 'B-', '096-322-159', '2025-10-29'),
 (5, 'PAT-0005', 'Ronald Lee', 'Male', 11, '2015-07-30', '088-588-614', 'ronald.lee68@example.com', 'Boston, MA', 'A+', '098-971-290', '2026-07-17'),
 (6, 'PAT-0006', 'George Thomas', 'Male', 55, '1971-07-05', '088-352-692', 'george.thomas77@example.com', 'Seattle, WA', 'A-', '070-773-697', '2025-11-05'),
 (7, 'PAT-0007', 'Cynthia Taylor', 'Female', 30, '1995-09-21', '096-344-371', 'cynthia.taylor51@example.com', 'Portland, OR', 'AB+', '070-423-869', '2026-07-15'),
 (8, 'PAT-0008', 'Steven Flores', 'Male', 76, '1950-07-20', '098-650-318', 'steven.flores65@example.com', 'Phoenix, AZ', 'B+', '096-170-350', '2026-02-13'),
 (9, 'PAT-0009', 'Barbara Lewis', 'Female', 73, '1952-09-13', '096-726-926', 'barbara.lewis84@example.com', 'Orlando, FL', 'A+', '096-779-206', '2026-06-14'),
 (10, 'PAT-0010', 'Linda Miller', 'Female', 74, '1952-06-21', '096-388-719', 'linda.miller27@example.com', 'Columbus, OH', 'B-', '096-617-600', '2026-04-15'),
 (11, 'PAT-0011', 'Robert Green', 'Male', 58, '1968-04-16', '098-103-441', 'robert.green99@example.com', 'Portland, OR', 'AB+', '012-859-552', '2025-11-12'),
 (12, 'PAT-0012', 'Kimberly Smith', 'Female', 18, '2008-07-18', '012-658-136', 'kimberly.smith48@example.com', 'Raleigh, NC', 'B+', '070-230-142', '2026-03-17'),
 (13, 'PAT-0013', 'Sharon Williams', 'Female', 49, '1977-05-18', '012-782-205', 'sharon.williams46@example.com', 'Raleigh, NC', 'O+', '012-342-985', '2026-05-30'),
 (14, 'PAT-0014', 'Jacob Clark', 'Male', 7, '2019-05-24', '096-901-521', 'jacob.clark86@example.com', 'Atlanta, GA', 'AB+', '012-906-818', '2026-06-27'),
 (15, 'PAT-0015', 'Laura Williams', 'Female', 64, '1962-05-16', '012-936-571', 'laura.williams45@example.com', 'Nashville, TN', 'B-', '012-124-775', '2026-05-15'),
 (16, 'PAT-0016', 'Laura Jones', 'Female', 39, '1987-03-05', '088-509-795', 'laura.jones69@example.com', 'Columbus, OH', 'A+', '098-998-367', '2026-05-22'),
 (17, 'PAT-0017', 'Patricia Miller', 'Female', 80, '1946-01-31', '096-846-905', 'patricia.miller41@example.com', 'Minneapolis, MN', 'A-', '070-690-294', '2026-04-13'),
 (18, 'PAT-0018', 'Timothy Ramirez', 'Male', 4, '2021-11-29', '088-803-836', 'timothy.ramirez95@example.com', 'Chicago, IL', 'AB-', '070-171-780', '2026-03-05'),
 (19, 'PAT-0019', 'Carol Davis', 'Female', 42, '1983-12-16', '096-782-518', 'carol.davis42@example.com', 'San Diego, CA', 'AB+', '012-296-530', '2025-09-15'),
 (20, 'PAT-0020', 'Carol Mitchell', 'Female', 26, '1999-10-17', '088-408-515', 'carol.mitchell71@example.com', 'Phnom Penh', 'AB+', '096-315-540', '2025-10-29'),
 (21, 'PAT-0021', 'Sandra Lewis', 'Female', 60, '1965-09-25', '012-623-584', 'sandra.lewis95@example.com', 'Boston, MA', 'A-', '096-627-779', '2025-10-01'),
 (22, 'PAT-0022', 'Jennifer Carter', 'Female', 34, '1991-09-20', '096-330-925', 'jennifer.carter26@example.com', 'Portland, OR', 'A+', '098-350-586', '2025-10-13'),
 (23, 'PAT-0023', 'Steven Clark', 'Male', 84, '1941-11-21', '012-835-813', 'steven.clark50@example.com', 'Sacramento, CA', 'O+', '012-251-771', '2025-09-03'),
 (24, 'PAT-0024', 'Jacob Carter', 'Male', 17, '2009-01-20', '012-280-923', 'jacob.carter90@example.com', 'Orlando, FL', 'O-', '098-670-355', '2026-06-20'),
 (25, 'PAT-0025', 'Elizabeth Robinson', 'Female', 89, '1936-12-15', '088-709-424', 'elizabeth.robinson97@example.com', 'Kansas City, MO', 'O+', '070-262-861', '2025-12-21'),
 (26, 'PAT-0026', 'Sarah Carter', 'Female', 35, '1990-10-08', '096-884-896', 'sarah.carter67@example.com', 'Sacramento, CA', 'B-', '096-550-179', '2025-08-21'),
 (27, 'PAT-0027', 'Jessica Moore', 'Female', 46, '1980-03-22', '088-182-241', 'jessica.moore20@example.com', 'Atlanta, GA', 'O+', '012-823-319', '2026-07-20'),
 (28, 'PAT-0028', 'Betty Perez', 'Female', 73, '1953-01-13', '070-163-311', 'betty.perez54@example.com', 'San Diego, CA', 'A+', '070-588-106', '2026-02-22'),
 (29, 'PAT-0029', 'Deborah Harris', 'Female', 57, '1968-12-03', '088-919-717', 'deborah.harris29@example.com', 'Sacramento, CA', 'B-', '096-546-597', '2026-08-07'),
 (30, 'PAT-0030', 'Nancy Nelson', 'Female', 55, '1971-06-12', '070-230-737', 'nancy.nelson69@example.com', 'Phnom Penh', 'O+', '098-185-758', '2026-01-14'),
 (31, 'PAT-0031', 'Ryan Robinson', 'Male', 27, '1999-08-03', '096-488-435', 'ryan.robinson28@example.com', 'Kansas City, MO', 'AB-', '096-879-488', '2026-04-01'),
 (32, 'PAT-0032', 'Sarah Garcia', 'Female', 64, '1962-08-28', '088-153-458', 'sarah.garcia29@example.com', 'Austin, TX', 'A+', '098-353-304', '2026-08-11'),
 (33, 'PAT-0033', 'Joseph Rodriguez', 'Male', 64, '1961-09-29', '098-677-323', 'joseph.rodriguez60@example.com', 'Phoenix, AZ', 'AB-', '012-720-721', '2025-08-04'),
 (34, 'PAT-0034', 'Edward Hernandez', 'Male', 43, '1983-07-08', '088-126-419', 'edward.hernandez74@example.com', 'San Diego, CA', 'O+', '012-177-706', '2025-09-02'),
 (35, 'PAT-0035', 'John Hall', 'Male', 42, '1983-09-16', '088-924-223', 'john.hall73@example.com', 'Seattle, WA', 'AB-', '070-777-479', '2026-07-17');

INSERT OR IGNORE INTO appointments (id, appointment_code, patient_id, doctor_id, department, appointment_date, appointment_time, status, reason)
VALUES
 (1, 'APT-00001', 10, 21, 'Psychiatry', '2026-08-26', '14:30:00', 'SCHEDULED', 'Follow-up visit'),
 (2, 'APT-00002', 35, 5, 'Cardiology', '2026-07-19', '10:30:00', 'COMPLETED', 'Allergy consultation'),
 (3, 'APT-00003', 3, 14, 'Cardiology', '2026-07-18', '11:00:00', 'COMPLETED', 'Vaccination'),
 (4, 'APT-00004', 28, 6, 'Pediatrics', '2026-07-14', '16:00:00', 'COMPLETED', 'Vaccination'),
 (5, 'APT-00005', 26, 4, 'General Medicine', '2026-07-13', '09:30:00', 'COMPLETED', 'Chest pain evaluation'),
 (6, 'APT-00006', 27, 19, 'Radiology', '2026-07-25', '13:30:00', 'COMPLETED', 'Medication review'),
 (7, 'APT-00007', 12, 36, 'Dermatology', '2026-07-20', '14:00:00', 'COMPLETED', 'Post-surgical follow-up'),
 (8, 'APT-00008', 7, 24, 'Pediatrics', '2026-07-15', '14:00:00', 'COMPLETED', 'Post-surgical follow-up'),
 (9, 'APT-00009', 35, 32, 'Cardiology', '2026-08-30', '15:30:00', 'SCHEDULED', 'Joint pain'),
 (10, 'APT-00010', 30, 30, 'Psychiatry', '2026-08-22', '10:00:00', 'SCHEDULED', 'Vaccination'),
 (11, 'APT-00011', 16, 12, 'Psychiatry', '2026-07-17', '14:00:00', 'COMPLETED', 'Sports injury'),
 (12, 'APT-00012', 29, 22, 'General Medicine', '2026-08-12', '14:00:00', 'NO_SHOW', 'Persistent headache'),
 (13, 'APT-00013', 27, 33, 'Pediatrics', '2026-07-28', '15:30:00', 'COMPLETED', 'Sports injury'),
 (14, 'APT-00014', 3, 27, 'Dermatology', '2026-07-16', '15:30:00', 'COMPLETED', 'Joint pain'),
 (15, 'APT-00015', 23, 22, 'General Medicine', '2026-09-08', '14:00:00', 'SCHEDULED', 'Prenatal checkup'),
 (16, 'APT-00016', 6, 5, 'Cardiology', '2026-08-10', '13:00:00', 'COMPLETED', 'Annual physical'),
 (17, 'APT-00017', 20, 4, 'General Medicine', '2026-09-02', '10:00:00', 'SCHEDULED', 'Fever and cough'),
 (18, 'APT-00018', 2, 23, 'Cardiology', '2026-09-04', '10:30:00', 'SCHEDULED', 'Skin rash'),
 (19, 'APT-00019', 32, 8, 'Neurology', '2026-07-14', '09:30:00', 'COMPLETED', 'Chest pain evaluation'),
 (20, 'APT-00020', 26, 16, 'Orthopedics', '2026-08-26', '16:00:00', 'SCHEDULED', 'Sports injury'),
 (21, 'APT-00021', 11, 6, 'Pediatrics', '2026-09-02', '11:00:00', 'SCHEDULED', 'Diabetes management'),
 (22, 'APT-00022', 9, 18, 'Dermatology', '2026-08-31', '16:00:00', 'SCHEDULED', 'Diabetes management'),
 (23, 'APT-00023', 27, 18, 'Dermatology', '2026-08-21', '14:30:00', 'CANCELLED', 'Vaccination'),
 (24, 'APT-00024', 6, 10, 'Radiology', '2026-07-29', '09:00:00', 'COMPLETED', 'Vaccination'),
 (25, 'APT-00025', 32, 1, 'Cardiology', '2026-07-30', '10:00:00', 'COMPLETED', 'Chest pain evaluation'),
 (26, 'APT-00026', 35, 27, 'Dermatology', '2026-08-23', '14:00:00', 'SCHEDULED', 'Blood pressure check'),
 (27, 'APT-00027', 9, 21, 'Psychiatry', '2026-09-10', '14:00:00', 'SCHEDULED', 'Follow-up visit'),
 (28, 'APT-00028', 26, 30, 'Psychiatry', '2026-08-26', '11:00:00', 'SCHEDULED', 'Fever and cough'),
 (29, 'APT-00029', 31, 7, 'Orthopedics', '2026-08-27', '08:00:00', 'SCHEDULED', 'Post-surgical follow-up'),
 (30, 'APT-00030', 14, 5, 'Cardiology', '2026-09-01', '09:00:00', 'SCHEDULED', 'Persistent headache'),
 (31, 'APT-00031', 4, 22, 'General Medicine', '2026-07-20', '08:00:00', 'COMPLETED', 'Diabetes management'),
 (32, 'APT-00032', 24, 7, 'Orthopedics', '2026-07-10', '08:30:00', 'CANCELLED', 'Wellness visit'),
 (33, 'APT-00033', 10, 25, 'Orthopedics', '2026-08-08', '10:30:00', 'COMPLETED', 'Sports injury'),
 (34, 'APT-00034', 8, 8, 'Neurology', '2026-09-07', '13:00:00', 'SCHEDULED', 'Sports injury'),
 (35, 'APT-00035', 20, 31, 'General Medicine', '2026-07-17', '09:00:00', 'COMPLETED', 'Joint pain'),
 (36, 'APT-00036', 31, 17, 'Neurology', '2026-07-27', '13:30:00', 'COMPLETED', 'Allergy consultation'),
 (37, 'APT-00037', 10, 24, 'Pediatrics', '2026-07-10', '15:30:00', 'COMPLETED', 'Annual physical'),
 (38, 'APT-00038', 34, 17, 'Neurology', '2026-08-22', '09:00:00', 'SCHEDULED', 'Shortness of breath'),
 (39, 'APT-00039', 35, 15, 'Pediatrics', '2026-09-09', '10:30:00', 'SCHEDULED', 'Vaccination'),
 (40, 'APT-00040', 16, 13, 'General Medicine', '2026-08-27', '15:00:00', 'SCHEDULED', 'Vaccination'),
 (41, 'APT-00041', 34, 13, 'General Medicine', '2026-09-08', '10:30:00', 'SCHEDULED', 'Routine checkup'),
 (42, 'APT-00042', 18, 2, 'Pediatrics', '2026-09-05', '10:00:00', 'SCHEDULED', 'Post-surgical follow-up'),
 (43, 'APT-00043', 29, 23, 'Cardiology', '2026-08-20', '10:30:00', 'SCHEDULED', 'Persistent headache'),
 (44, 'APT-00044', 31, 15, 'Pediatrics', '2026-08-01', '10:30:00', 'COMPLETED', 'Wellness visit'),
 (45, 'APT-00045', 31, 1, 'Cardiology', '2026-08-20', '15:30:00', 'COMPLETED', 'Persistent headache'),
 (46, 'APT-00046', 13, 25, 'Orthopedics', '2026-09-06', '09:00:00', 'SCHEDULED', 'Anxiety consultation'),
 (47, 'APT-00047', 6, 22, 'General Medicine', '2026-08-26', '13:00:00', 'SCHEDULED', 'Fever and cough'),
 (48, 'APT-00048', 11, 6, 'Pediatrics', '2026-07-28', '09:00:00', 'COMPLETED', 'Blood pressure check'),
 (49, 'APT-00049', 10, 30, 'Psychiatry', '2026-09-05', '14:30:00', 'SCHEDULED', 'Shortness of breath'),
 (50, 'APT-00050', 9, 10, 'Radiology', '2026-07-09', '08:00:00', 'COMPLETED', 'Persistent headache'),
 (51, 'APT-00051', 9, 34, 'Orthopedics', '2026-08-31', '16:00:00', 'SCHEDULED', 'Post-surgical follow-up'),
 (52, 'APT-00052', 2, 14, 'Cardiology', '2026-08-08', '09:30:00', 'COMPLETED', 'Vaccination'),
 (53, 'APT-00053', 17, 21, 'Psychiatry', '2026-08-29', '16:00:00', 'SCHEDULED', 'Chest pain evaluation'),
 (54, 'APT-00054', 23, 4, 'General Medicine', '2026-09-03', '14:30:00', 'SCHEDULED', 'Blood pressure check'),
 (55, 'APT-00055', 27, 34, 'Orthopedics', '2026-09-09', '09:00:00', 'SCHEDULED', 'Diabetes management'),
 (56, 'APT-00056', 34, 10, 'Radiology', '2026-09-10', '08:00:00', 'SCHEDULED', 'Prenatal checkup'),
 (57, 'APT-00057', 1, 12, 'Psychiatry', '2026-07-26', '09:00:00', 'COMPLETED', 'Wellness visit'),
 (58, 'APT-00058', 4, 8, 'Neurology', '2026-08-17', '14:30:00', 'COMPLETED', 'Diabetes management'),
 (59, 'APT-00059', 7, 31, 'General Medicine', '2026-07-14', '09:30:00', 'COMPLETED', 'Follow-up visit'),
 (60, 'APT-00060', 33, 7, 'Orthopedics', '2026-09-02', '13:30:00', 'SCHEDULED', 'Routine checkup'),
 (61, 'APT-00061', 29, 5, 'Cardiology', '2026-08-17', '14:00:00', 'NO_SHOW', 'Wellness visit'),
 (62, 'APT-00062', 13, 33, 'Pediatrics', '2026-08-11', '13:00:00', 'COMPLETED', 'Sports injury'),
 (63, 'APT-00063', 16, 33, 'Pediatrics', '2026-08-09', '13:30:00', 'CANCELLED', 'Post-surgical follow-up'),
 (64, 'APT-00064', 9, 29, 'Surgery', '2026-08-29', '08:30:00', 'SCHEDULED', 'Fever and cough'),
 (65, 'APT-00065', 21, 29, 'Surgery', '2026-07-16', '14:30:00', 'COMPLETED', 'Annual physical'),
 (66, 'APT-00066', 20, 14, 'Cardiology', '2026-07-22', '15:30:00', 'COMPLETED', 'Shortness of breath'),
 (67, 'APT-00067', 17, 10, 'Radiology', '2026-07-24', '13:00:00', 'COMPLETED', 'Persistent headache'),
 (68, 'APT-00068', 32, 26, 'Neurology', '2026-07-27', '14:30:00', 'CANCELLED', 'Skin rash'),
 (69, 'APT-00069', 33, 28, 'Radiology', '2026-08-27', '10:30:00', 'SCHEDULED', 'Anxiety consultation'),
 (70, 'APT-00070', 23, 13, 'General Medicine', '2026-08-16', '08:30:00', 'COMPLETED', 'Routine checkup'),
 (71, 'APT-00071', 30, 22, 'General Medicine', '2026-09-01', '15:00:00', 'SCHEDULED', 'Routine checkup'),
 (72, 'APT-00072', 22, 25, 'Orthopedics', '2026-08-13', '13:30:00', 'NO_SHOW', 'Persistent headache'),
 (73, 'APT-00073', 7, 15, 'Pediatrics', '2026-07-17', '10:00:00', 'COMPLETED', 'Skin rash'),
 (74, 'APT-00074', 9, 18, 'Dermatology', '2026-08-30', '16:00:00', 'SCHEDULED', 'Lab result review'),
 (75, 'APT-00075', 10, 26, 'Neurology', '2026-09-10', '14:00:00', 'SCHEDULED', 'Sports injury'),
 (76, 'APT-00076', 6, 21, 'Psychiatry', '2026-08-11', '08:00:00', 'COMPLETED', 'Skin rash'),
 (77, 'APT-00077', 5, 28, 'Radiology', '2026-08-10', '08:00:00', 'COMPLETED', 'Lab result review'),
 (78, 'APT-00078', 15, 6, 'Pediatrics', '2026-07-15', '10:00:00', 'CANCELLED', 'Prenatal checkup'),
 (79, 'APT-00079', 22, 1, 'Cardiology', '2026-08-29', '10:00:00', 'SCHEDULED', 'Wellness visit'),
 (80, 'APT-00080', 3, 9, 'Dermatology', '2026-08-06', '08:30:00', 'NO_SHOW', 'Lab result review'),
 (81, 'APT-00081', 12, 4, 'General Medicine', '2026-08-01', '10:00:00', 'COMPLETED', 'Allergy consultation'),
 (82, 'APT-00082', 19, 14, 'Cardiology', '2026-09-02', '13:30:00', 'SCHEDULED', 'Skin rash'),
 (83, 'APT-00083', 23, 18, 'Dermatology', '2026-07-09', '10:00:00', 'COMPLETED', 'Routine checkup'),
 (84, 'APT-00084', 13, 33, 'Pediatrics', '2026-09-10', '13:00:00', 'SCHEDULED', 'Vaccination'),
 (85, 'APT-00085', 7, 29, 'Surgery', '2026-08-31', '14:30:00', 'SCHEDULED', 'Sports injury'),
 (86, 'APT-00086', 26, 35, 'Neurology', '2026-09-09', '10:00:00', 'SCHEDULED', 'Post-surgical follow-up'),
 (87, 'APT-00087', 22, 15, 'Pediatrics', '2026-08-01', '16:00:00', 'CANCELLED', 'Chest pain evaluation'),
 (88, 'APT-00088', 23, 26, 'Neurology', '2026-07-13', '16:00:00', 'COMPLETED', 'Annual physical'),
 (89, 'APT-00089', 28, 17, 'Neurology', '2026-07-27', '08:00:00', 'COMPLETED', 'Fever and cough'),
 (90, 'APT-00090', 19, 33, 'Pediatrics', '2026-08-07', '15:00:00', 'COMPLETED', 'Prenatal checkup'),
 (91, 'APT-00091', 11, 12, 'Psychiatry', '2026-08-10', '13:00:00', 'COMPLETED', 'Shortness of breath'),
 (92, 'APT-00092', 21, 22, 'General Medicine', '2026-08-07', '08:00:00', 'NO_SHOW', 'Medication review'),
 (93, 'APT-00093', 23, 14, 'Cardiology', '2026-07-30', '08:00:00', 'COMPLETED', 'Annual physical'),
 (94, 'APT-00094', 18, 31, 'General Medicine', '2026-09-09', '14:30:00', 'SCHEDULED', 'Post-surgical follow-up'),
 (95, 'APT-00095', 33, 16, 'Orthopedics', '2026-07-07', '08:30:00', 'COMPLETED', 'Annual physical'),
 (96, 'APT-00096', 26, 10, 'Radiology', '2026-07-12', '11:00:00', 'COMPLETED', 'Medication review'),
 (97, 'APT-00097', 6, 15, 'Pediatrics', '2026-07-26', '14:30:00', 'CANCELLED', 'Wellness visit'),
 (98, 'APT-00098', 21, 25, 'Orthopedics', '2026-09-08', '09:00:00', 'SCHEDULED', 'Medication review'),
 (99, 'APT-00099', 3, 10, 'Radiology', '2026-09-10', '14:30:00', 'SCHEDULED', 'Anxiety consultation'),
 (100, 'APT-00100', 9, 33, 'Pediatrics', '2026-09-09', '14:00:00', 'SCHEDULED', 'Routine checkup'),
 (101, 'APT-00101', 6, 15, 'Pediatrics', '2026-07-10', '08:00:00', 'COMPLETED', 'Shortness of breath'),
 (102, 'APT-00102', 2, 4, 'General Medicine', '2026-08-07', '13:00:00', 'COMPLETED', 'Prenatal checkup'),
 (103, 'APT-00103', 33, 5, 'Cardiology', '2026-07-18', '14:30:00', 'COMPLETED', 'Sports injury'),
 (104, 'APT-00104', 5, 17, 'Neurology', '2026-08-09', '09:30:00', 'COMPLETED', 'Post-surgical follow-up'),
 (105, 'APT-00105', 30, 15, 'Pediatrics', '2026-09-08', '16:00:00', 'SCHEDULED', 'Fever and cough'),
 (106, 'APT-00106', 31, 5, 'Cardiology', '2026-08-12', '15:30:00', 'COMPLETED', 'Post-surgical follow-up'),
 (107, 'APT-00107', 10, 5, 'Cardiology', '2026-08-18', '10:00:00', 'COMPLETED', 'Medication review'),
 (108, 'APT-00108', 1, 9, 'Dermatology', '2026-09-06', '08:00:00', 'SCHEDULED', 'Sports injury'),
 (109, 'APT-00109', 7, 18, 'Dermatology', '2026-08-03', '14:30:00', 'COMPLETED', 'Allergy consultation'),
 (110, 'APT-00110', 30, 19, 'Radiology', '2026-09-04', '13:00:00', 'SCHEDULED', 'Persistent headache'),
 (111, 'APT-00111', 13, 36, 'Dermatology', '2026-08-15', '08:30:00', 'NO_SHOW', 'Routine checkup'),
 (112, 'APT-00112', 30, 19, 'Radiology', '2026-07-16', '16:00:00', 'COMPLETED', 'Prenatal checkup'),
 (113, 'APT-00113', 25, 18, 'Dermatology', '2026-08-02', '09:30:00', 'COMPLETED', 'Annual physical'),
 (114, 'APT-00114', 34, 10, 'Radiology', '2026-08-09', '10:30:00', 'COMPLETED', 'Allergy consultation'),
 (115, 'APT-00115', 8, 18, 'Dermatology', '2026-08-22', '09:30:00', 'SCHEDULED', 'Sports injury'),
 (116, 'APT-00116', 26, 32, 'Cardiology', '2026-07-10', '09:00:00', 'COMPLETED', 'Sports injury'),
 (117, 'APT-00117', 26, 29, 'Surgery', '2026-08-14', '15:00:00', 'COMPLETED', 'Shortness of breath'),
 (118, 'APT-00118', 21, 25, 'Orthopedics', '2026-07-22', '16:00:00', 'COMPLETED', 'Joint pain'),
 (119, 'APT-00119', 26, 22, 'General Medicine', '2026-07-22', '09:30:00', 'COMPLETED', 'Medication review');

INSERT OR IGNORE INTO medical_records (id, appointment_id, patient_id, doctor_id, diagnosis, symptoms, prescription, lab_result, doctor_notes, record_date)
VALUES
 (1, 2, 35, 5, 'Osteoarthritis, Knee', 'Joint stiffness, pain on weight-bearing', 'Acetaminophen, physical therapy referral', 'Chest X-ray: no acute findings', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-19'),
 (2, 3, 3, 14, 'Acute Bronchitis', 'Persistent cough, chest discomfort, low-grade fever', 'Guaifenesin, rest, follow up if symptoms worsen', 'Fasting glucose 118 mg/dL - borderline', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-18'),
 (3, 4, 28, 6, 'Urinary Tract Infection', 'Dysuria, urinary frequency', 'Nitrofurantoin 100mg twice daily for 5 days', 'Fasting glucose 118 mg/dL - borderline', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-14'),
 (4, 6, 27, 19, 'Community-Acquired Pneumonia', 'Productive cough, fever, chest X-ray shows infiltrate', 'Amoxicillin-clavulanate 875mg twice daily for 7 days', 'Lipid panel: LDL 142 mg/dL, elevated', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-25'),
 (5, 7, 12, 36, 'Gastroesophageal Reflux Disease', 'Heartburn after meals, occasional regurgitation', 'Omeprazole 20mg once daily before breakfast', 'HbA1c 6.8% - prediabetic range', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-20'),
 (6, 11, 16, 12, 'Generalized Anxiety with Insomnia', 'Difficulty falling asleep, racing thoughts', 'Sleep hygiene counseling, consider short-term melatonin', 'Chest X-ray: no acute findings', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-17'),
 (7, 13, 27, 33, 'Well-Child Visit', 'Growth and development on track', 'Routine vaccinations administered per schedule', 'Fasting glucose 118 mg/dL - borderline', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-28'),
 (8, 16, 6, 5, 'Hypertension, Stage 1', 'Occasional headaches, elevated BP reading 138/88', 'Lisinopril 10mg once daily, recheck in 4 weeks', 'None ordered this visit', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-10'),
 (9, 19, 32, 8, 'Seasonal Allergic Rhinitis', 'Sneezing, itchy eyes, runny nose', 'Cetirizine 10mg once daily', 'Urinalysis: positive for leukocyte esterase', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-14'),
 (10, 24, 6, 10, 'Community-Acquired Pneumonia', 'Productive cough, fever, chest X-ray shows infiltrate', 'Amoxicillin-clavulanate 875mg twice daily for 7 days', 'Lipid panel: LDL 142 mg/dL, elevated', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-29'),
 (11, 25, 32, 1, 'Acute Sinusitis', 'Facial pressure, nasal discharge, 8 days duration', 'Amoxicillin 500mg three times daily for 10 days', 'Fasting glucose 118 mg/dL - borderline', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-30'),
 (12, 31, 4, 22, 'Osteoarthritis, Knee', 'Joint stiffness, pain on weight-bearing', 'Acetaminophen, physical therapy referral', 'None ordered this visit', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-20'),
 (13, 35, 20, 31, 'Osteoarthritis, Knee', 'Joint stiffness, pain on weight-bearing', 'Acetaminophen, physical therapy referral', 'Chest X-ray: no acute findings', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-17'),
 (14, 36, 31, 17, 'Generalized Anxiety with Insomnia', 'Difficulty falling asleep, racing thoughts', 'Sleep hygiene counseling, consider short-term melatonin', 'Chest X-ray: no acute findings', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-27'),
 (15, 44, 31, 15, 'Seasonal Allergic Rhinitis', 'Sneezing, itchy eyes, runny nose', 'Cetirizine 10mg once daily', 'Fasting glucose 118 mg/dL - borderline', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-01'),
 (16, 45, 31, 1, 'Sports-Related Ankle Sprain', 'Grade 2 lateral ankle sprain, swelling present', 'RICE protocol, ankle brace, physical therapy', 'HbA1c 6.8% - prediabetic range', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-20'),
 (17, 48, 11, 6, 'Community-Acquired Pneumonia', 'Productive cough, fever, chest X-ray shows infiltrate', 'Amoxicillin-clavulanate 875mg twice daily for 7 days', 'Fasting glucose 118 mg/dL - borderline', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-28'),
 (18, 50, 9, 10, 'Contact Dermatitis', 'Localized rash after new soap use', 'Discontinue irritant, topical steroid cream short course', 'CBC within normal limits', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-09'),
 (19, 52, 2, 14, 'Acute Bronchitis', 'Persistent cough, chest discomfort, low-grade fever', 'Guaifenesin, rest, follow up if symptoms worsen', 'HbA1c 6.8% - prediabetic range', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-08'),
 (20, 57, 1, 12, 'Seasonal Allergic Rhinitis', 'Sneezing, itchy eyes, runny nose', 'Cetirizine 10mg once daily', 'Lipid panel: LDL 142 mg/dL, elevated', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-26'),
 (21, 58, 4, 8, 'Acute Sinusitis', 'Facial pressure, nasal discharge, 8 days duration', 'Amoxicillin 500mg three times daily for 10 days', 'HbA1c 6.8% - prediabetic range', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-17'),
 (22, 59, 7, 31, 'Acute Bronchitis', 'Persistent cough, chest discomfort, low-grade fever', 'Guaifenesin, rest, follow up if symptoms worsen', 'TSH 2.1 mIU/L - normal', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-14'),
 (23, 65, 21, 29, 'Seasonal Allergic Rhinitis', 'Sneezing, itchy eyes, runny nose', 'Cetirizine 10mg once daily', 'Chest X-ray: no acute findings', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-16'),
 (24, 66, 20, 14, 'Contact Dermatitis', 'Localized rash after new soap use', 'Discontinue irritant, topical steroid cream short course', 'Chest X-ray: no acute findings', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-22'),
 (25, 67, 17, 10, 'Community-Acquired Pneumonia', 'Productive cough, fever, chest X-ray shows infiltrate', 'Amoxicillin-clavulanate 875mg twice daily for 7 days', 'Fasting glucose 118 mg/dL - borderline', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-24'),
 (26, 70, 23, 13, 'Acute Bronchitis', 'Persistent cough, chest discomfort, low-grade fever', 'Guaifenesin, rest, follow up if symptoms worsen', 'Urinalysis: positive for leukocyte esterase', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-16'),
 (27, 73, 7, 15, 'Migraine without Aura', 'Unilateral throbbing headache, photophobia', 'Sumatriptan as needed, avoid known triggers', 'HbA1c 6.8% - prediabetic range', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-17'),
 (28, 76, 6, 21, 'Osteoarthritis, Knee', 'Joint stiffness, pain on weight-bearing', 'Acetaminophen, physical therapy referral', 'TSH 2.1 mIU/L - normal', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-11'),
 (29, 77, 5, 28, 'Atopic Dermatitis', 'Dry, itchy patches on forearms', 'Topical hydrocortisone cream, moisturizer', 'Urinalysis: positive for leukocyte esterase', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-10'),
 (30, 81, 12, 4, 'Gastroesophageal Reflux Disease', 'Heartburn after meals, occasional regurgitation', 'Omeprazole 20mg once daily before breakfast', 'Chest X-ray: no acute findings', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-01'),
 (31, 83, 23, 18, 'Iron Deficiency Anemia', 'Fatigue, pallor, low ferritin on labs', 'Ferrous sulfate 325mg once daily with vitamin C', 'Urinalysis: positive for leukocyte esterase', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-09'),
 (32, 88, 23, 26, 'Seasonal Allergic Rhinitis', 'Sneezing, itchy eyes, runny nose', 'Cetirizine 10mg once daily', 'TSH 2.1 mIU/L - normal', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-13'),
 (33, 89, 28, 17, 'Urinary Tract Infection', 'Dysuria, urinary frequency', 'Nitrofurantoin 100mg twice daily for 5 days', 'TSH 2.1 mIU/L - normal', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-27'),
 (34, 90, 19, 33, 'Urinary Tract Infection', 'Dysuria, urinary frequency', 'Nitrofurantoin 100mg twice daily for 5 days', 'HbA1c 6.8% - prediabetic range', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-07'),
 (35, 91, 11, 12, 'Gastroesophageal Reflux Disease', 'Heartburn after meals, occasional regurgitation', 'Omeprazole 20mg once daily before breakfast', 'Fasting glucose 118 mg/dL - borderline', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-10'),
 (36, 93, 23, 14, 'Seasonal Allergic Rhinitis', 'Sneezing, itchy eyes, runny nose', 'Cetirizine 10mg once daily', 'TSH 2.1 mIU/L - normal', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-30'),
 (37, 95, 33, 16, 'Urinary Tract Infection', 'Dysuria, urinary frequency', 'Nitrofurantoin 100mg twice daily for 5 days', 'None ordered this visit', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-07'),
 (38, 96, 26, 10, 'Atopic Dermatitis', 'Dry, itchy patches on forearms', 'Topical hydrocortisone cream, moisturizer', 'Urinalysis: positive for leukocyte esterase', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-12'),
 (39, 101, 6, 15, 'Well-Child Visit', 'Growth and development on track', 'Routine vaccinations administered per schedule', 'Lipid panel: LDL 142 mg/dL, elevated', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-10'),
 (40, 102, 2, 4, 'Atopic Dermatitis', 'Dry, itchy patches on forearms', 'Topical hydrocortisone cream, moisturizer', 'HbA1c 6.8% - prediabetic range', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-07'),
 (41, 103, 33, 5, 'Hypertension, Stage 1', 'Occasional headaches, elevated BP reading 138/88', 'Lisinopril 10mg once daily, recheck in 4 weeks', 'None ordered this visit', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-18'),
 (42, 104, 5, 17, 'Anxiety Disorder', 'Reports of persistent worry, difficulty sleeping', 'Cognitive behavioral therapy referral, follow-up in 2 weeks', 'Fasting glucose 118 mg/dL - borderline', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-09'),
 (43, 106, 31, 5, 'Well-Child Visit', 'Growth and development on track', 'Routine vaccinations administered per schedule', 'None ordered this visit', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-12'),
 (44, 107, 10, 5, 'Anxiety Disorder', 'Reports of persistent worry, difficulty sleeping', 'Cognitive behavioral therapy referral, follow-up in 2 weeks', 'HbA1c 6.8% - prediabetic range', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-18'),
 (45, 112, 30, 19, 'Contact Dermatitis', 'Localized rash after new soap use', 'Discontinue irritant, topical steroid cream short course', 'Chest X-ray: no acute findings', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-16'),
 (46, 113, 25, 18, 'Contact Dermatitis', 'Localized rash after new soap use', 'Discontinue irritant, topical steroid cream short course', 'None ordered this visit', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-02'),
 (47, 114, 34, 10, 'Well-Child Visit', 'Growth and development on track', 'Routine vaccinations administered per schedule', 'Lipid panel: LDL 142 mg/dL, elevated', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-08-09'),
 (48, 116, 26, 32, 'Gastroesophageal Reflux Disease', 'Heartburn after meals, occasional regurgitation', 'Omeprazole 20mg once daily before breakfast', 'Urinalysis: positive for leukocyte esterase', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-10'),
 (49, 118, 21, 25, 'Migraine without Aura', 'Unilateral throbbing headache, photophobia', 'Sumatriptan as needed, avoid known triggers', 'Urinalysis: positive for leukocyte esterase', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-22'),
 (50, 119, 26, 22, 'Atopic Dermatitis', 'Dry, itchy patches on forearms', 'Topical hydrocortisone cream, moisturizer', 'HbA1c 6.8% - prediabetic range', 'Patient tolerated visit well. Advised to return if symptoms persist or worsen.', '2026-07-22');

INSERT OR IGNORE INTO invoices (id, invoice_code, patient_id, appointment_id, consultation_fee, medicine_fee, lab_fee, discount, tax, total, payment_status, issued_on)
VALUES
 (1, 'INV-00001', 35, 2, 27.07, 0, 35.0, 0.0, 3.1, 65.17, 'UNPAID', '2026-07-19'),
 (2, 'INV-00002', 3, 3, 36.12, 12.0, 0, 0.0, 2.41, 50.53, 'PAID', '2026-07-18'),
 (3, 'INV-00003', 28, 4, 71.54, 22.0, 35.0, 0.0, 6.43, 134.97, 'PARTIAL', '2026-07-14'),
 (4, 'INV-00004', 26, 5, 42.88, 8.5, 20.0, 0.0, 3.57, 74.95, 'PAID', '2026-07-13'),
 (5, 'INV-00005', 27, 6, 89.83, 35.5, 20.0, 0.0, 7.27, 152.6, 'PAID', '2026-07-25'),
 (6, 'INV-00006', 12, 7, 72.33, 15.75, 0, 8.81, 3.96, 83.23, 'PAID', '2026-07-20'),
 (7, 'INV-00007', 7, 8, 36.61, 0, 35.0, 3.58, 3.4, 71.43, 'PAID', '2026-07-15'),
 (8, 'INV-00008', 16, 11, 81.32, 0, 0, 0.0, 4.07, 85.39, 'PAID', '2026-07-17'),
 (9, 'INV-00009', 27, 13, 43.1, 0, 35.0, 3.9, 3.71, 77.91, 'PAID', '2026-07-28'),
 (10, 'INV-00010', 3, 14, 86.55, 12.0, 48.5, 0.0, 7.35, 154.4, 'UNPAID', '2026-07-16'),
 (11, 'INV-00011', 6, 16, 27.07, 12.0, 65.0, 5.2, 4.94, 103.81, 'PAID', '2026-08-10'),
 (12, 'INV-00012', 32, 19, 35.11, 22.0, 90.0, 14.71, 6.62, 139.02, 'PAID', '2026-07-14'),
 (13, 'INV-00013', 6, 24, 88.25, 12.0, 0, 5.01, 4.76, 100.0, 'PARTIAL', '2026-07-29'),
 (14, 'INV-00014', 32, 25, 45.0, 35.5, 65.0, 7.28, 6.91, 145.13, 'UNPAID', '2026-07-30'),
 (15, 'INV-00015', 4, 31, 32.45, 35.5, 90.0, 15.79, 7.11, 149.27, 'PARTIAL', '2026-07-20'),
 (16, 'INV-00016', 10, 33, 74.56, 15.75, 65.0, 7.77, 7.38, 154.92, 'PAID', '2026-08-08'),
 (17, 'INV-00017', 20, 35, 26.37, 0, 90.0, 0.0, 5.82, 122.19, 'UNPAID', '2026-07-17'),
 (18, 'INV-00018', 31, 36, 57.45, 0, 48.5, 0.0, 5.3, 111.25, 'PAID', '2026-07-27'),
 (19, 'INV-00019', 10, 37, 36.61, 12.0, 65.0, 5.68, 5.4, 113.33, 'PARTIAL', '2026-07-10'),
 (20, 'INV-00020', 31, 44, 69.73, 35.5, 48.5, 7.69, 7.3, 153.34, 'PAID', '2026-08-01'),
 (21, 'INV-00021', 31, 45, 45.0, 12.0, 0, 0.0, 2.85, 59.85, 'PAID', '2026-08-20'),
 (22, 'INV-00022', 11, 48, 71.54, 42.0, 90.0, 0.0, 10.18, 213.72, 'PAID', '2026-07-28'),
 (23, 'INV-00023', 9, 50, 88.25, 12.0, 20.0, 6.01, 5.71, 119.95, 'PAID', '2026-07-09'),
 (24, 'INV-00024', 2, 52, 36.12, 35.5, 0, 0.0, 3.58, 75.2, 'PARTIAL', '2026-08-08'),
 (25, 'INV-00025', 1, 57, 81.32, 42.0, 90.0, 0.0, 10.67, 223.99, 'PAID', '2026-07-26'),
 (26, 'INV-00026', 4, 58, 35.11, 8.5, 20.0, 6.36, 2.86, 60.11, 'PAID', '2026-08-17'),
 (27, 'INV-00027', 7, 59, 26.37, 42.0, 48.5, 0.0, 5.84, 122.71, 'PAID', '2026-07-14'),
 (28, 'INV-00028', 13, 62, 43.1, 15.75, 20.0, 0.0, 3.94, 82.79, 'PAID', '2026-08-11'),
 (29, 'INV-00029', 21, 65, 74.09, 12.0, 90.0, 0.0, 8.8, 184.89, 'PARTIAL', '2026-07-16'),
 (30, 'INV-00030', 20, 66, 36.12, 35.5, 48.5, 0.0, 6.01, 126.13, 'PAID', '2026-07-22'),
 (31, 'INV-00031', 17, 67, 88.25, 0, 65.0, 0.0, 7.66, 160.91, 'UNPAID', '2026-07-24'),
 (32, 'INV-00032', 23, 70, 48.09, 0, 48.5, 0.0, 4.83, 101.42, 'UNPAID', '2026-08-16'),
 (33, 'INV-00033', 7, 73, 69.73, 15.75, 0, 8.55, 3.85, 80.78, 'UNPAID', '2026-07-17'),
 (34, 'INV-00034', 6, 76, 63.73, 15.75, 0, 0.0, 3.97, 83.45, 'PAID', '2026-08-11'),
 (35, 'INV-00035', 5, 77, 55.89, 15.75, 0, 7.16, 3.22, 67.7, 'PAID', '2026-08-10'),
 (36, 'INV-00036', 12, 81, 42.88, 12.0, 0, 0.0, 2.74, 57.62, 'PARTIAL', '2026-08-01'),
 (37, 'INV-00037', 23, 83, 61.49, 15.75, 0, 0.0, 3.86, 81.1, 'PAID', '2026-07-09'),
 (38, 'INV-00038', 23, 88, 32.27, 8.5, 90.0, 0.0, 6.54, 137.31, 'UNPAID', '2026-07-13'),
 (39, 'INV-00039', 28, 89, 57.45, 35.5, 0, 9.29, 4.18, 87.84, 'PAID', '2026-07-27'),
 (40, 'INV-00040', 19, 90, 43.1, 12.0, 20.0, 0.0, 3.75, 78.85, 'UNPAID', '2026-08-07'),
 (41, 'INV-00041', 11, 91, 81.32, 15.75, 35.0, 0.0, 6.6, 138.67, 'PAID', '2026-08-10'),
 (42, 'INV-00042', 23, 93, 36.12, 12.0, 65.0, 0.0, 5.66, 118.78, 'UNPAID', '2026-07-30'),
 (43, 'INV-00043', 33, 95, 77.33, 42.0, 65.0, 9.22, 8.76, 183.87, 'PAID', '2026-07-07'),
 (44, 'INV-00044', 26, 96, 88.25, 35.5, 20.0, 7.19, 6.83, 143.39, 'UNPAID', '2026-07-12'),
 (45, 'INV-00045', 6, 101, 69.73, 12.0, 48.5, 6.51, 6.19, 129.91, 'PAID', '2026-07-10'),
 (46, 'INV-00046', 2, 102, 42.88, 42.0, 0, 0.0, 4.24, 89.12, 'PAID', '2026-08-07'),
 (47, 'INV-00047', 33, 103, 27.07, 42.0, 65.0, 13.41, 6.03, 126.69, 'PAID', '2026-07-18'),
 (48, 'INV-00048', 5, 104, 57.45, 12.0, 65.0, 0.0, 6.72, 141.17, 'PAID', '2026-08-09'),
 (49, 'INV-00049', 31, 106, 27.07, 12.0, 20.0, 5.91, 2.66, 55.82, 'PAID', '2026-08-12'),
 (50, 'INV-00050', 10, 107, 27.07, 12.0, 65.0, 0.0, 5.2, 109.27, 'PAID', '2026-08-18'),
 (51, 'INV-00051', 7, 109, 61.49, 12.0, 35.0, 5.42, 5.15, 108.22, 'PAID', '2026-08-03'),
 (52, 'INV-00052', 30, 112, 89.83, 22.0, 0, 0.0, 5.59, 117.42, 'PAID', '2026-07-16'),
 (53, 'INV-00053', 25, 113, 61.49, 42.0, 0, 0.0, 5.17, 108.66, 'PAID', '2026-08-02'),
 (54, 'INV-00054', 34, 114, 88.25, 8.5, 0, 9.68, 4.35, 91.42, 'PAID', '2026-08-09'),
 (55, 'INV-00055', 26, 116, 66.02, 12.0, 65.0, 14.3, 6.44, 135.16, 'PAID', '2026-07-10'),
 (56, 'INV-00056', 26, 117, 74.09, 42.0, 65.0, 0.0, 9.05, 190.14, 'UNPAID', '2026-08-14'),
 (57, 'INV-00057', 21, 118, 74.56, 15.75, 48.5, 6.94, 6.59, 138.46, 'UNPAID', '2026-07-22'),
 (58, 'INV-00058', 26, 119, 32.45, 12.0, 90.0, 13.45, 6.05, 127.05, 'PARTIAL', '2026-07-22');

-- Admit a handful of patients into rooms for a realistic partial-occupancy
-- demo. Guarded with room_id IS NULL so this never re-admits a patient a
-- real user has since discharged via the UI.
UPDATE patients SET room_id = 1 WHERE id = 3 AND room_id IS NULL;
UPDATE patients SET room_id = 1 WHERE id = 9 AND room_id IS NULL;
UPDATE patients SET room_id = 1 WHERE id = 17 AND room_id IS NULL;
UPDATE patients SET room_id = 2 WHERE id = 25 AND room_id IS NULL;
UPDATE patients SET room_id = 3 WHERE id = 23 AND room_id IS NULL;
UPDATE patients SET room_id = 4 WHERE id = 30 AND room_id IS NULL;
