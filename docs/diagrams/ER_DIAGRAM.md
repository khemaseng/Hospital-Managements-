# Entity-Relationship Diagram

Matches both `src/main/resources/sql/schema.sqlite.sql` (runtime) and `sql/mysql_schema.sql` (production).

```mermaid
erDiagram
    USERS {
        int id PK
        string username UK
        string password_hash
        string role
        string full_name
        string email
        bool active
    }

    DOCTORS {
        int id PK
        string doctor_code UK
        string full_name
        string department
        string specialization
        string phone
        string email
        string working_schedule
        decimal consultation_fee
        int user_id FK
    }

    PATIENTS {
        int id PK
        string patient_code UK
        string full_name
        string gender
        int age
        date date_of_birth
        string phone
        string email
        string address
        string blood_group
        string emergency_contact
        date registered_on
    }

    APPOINTMENTS {
        int id PK
        string appointment_code UK
        int patient_id FK
        int doctor_id FK
        string department
        date appointment_date
        time appointment_time
        string status
        string reason
    }

    MEDICAL_RECORDS {
        int id PK
        int appointment_id FK
        int patient_id FK
        int doctor_id FK
        text diagnosis
        text symptoms
        text prescription
        text lab_result
        text doctor_notes
        date record_date
    }

    INVOICES {
        int id PK
        string invoice_code UK
        int patient_id FK
        int appointment_id FK
        decimal consultation_fee
        decimal medicine_fee
        decimal lab_fee
        decimal discount
        decimal tax
        decimal total
        date issued_on
    }

    USERS ||--o{ DOCTORS : "optional login for"
    PATIENTS ||--o{ APPOINTMENTS : "books"
    DOCTORS  ||--o{ APPOINTMENTS : "attends"
    PATIENTS ||--o{ MEDICAL_RECORDS : "has"
    DOCTORS  ||--o{ MEDICAL_RECORDS : "writes"
    APPOINTMENTS ||--o| MEDICAL_RECORDS : "produces"
    PATIENTS ||--o{ INVOICES : "billed"
    APPOINTMENTS ||--o| INVOICES : "generates"
```

## Constraints worth calling out

- `appointments` has `UNIQUE (doctor_id, appointment_date, appointment_time)` — the database-level backstop for the no-double-booking rule (the primary check happens in `AppointmentService.bookAppointment()`).
- All foreign keys from `appointments`/`medical_records`/`invoices` to `patients`/`doctors` cascade or null-out on delete, matching the code's confirm-before-delete UX in `PatientController`/`DoctorController`.
- Indexes: `patients.full_name`, `doctors.department`, `appointments.appointment_date`, and a composite `(doctor_id, appointment_date)` index support the search/filter/sort operations used throughout the UI.
