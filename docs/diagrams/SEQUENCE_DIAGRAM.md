# Sequence Diagram: Booking an Appointment

The most business-rule-heavy flow in the system — chosen because it best shows the
Controller → Service → Repository → Database layering plus the conflict-prevention rule.

```mermaid
sequenceDiagram
    actor U as User (Receptionist/Admin)
    participant FC as AppointmentFormController
    participant AS as AppointmentService
    participant AR as AppointmentRepository
    participant DB as DatabaseConnection (SQLite)

    U->>FC: Fill form, click "Book Appointment"
    FC->>FC: Read patient, doctor, date, time, reason
    FC->>AS: bookAppointment(appointment)
    activate AS

    AS->>AS: validate(appointment)
    alt validation fails (missing patient/doctor/date/time)
        AS-->>FC: throw ValidationException
        FC-->>U: Show inline error label
    else validation passes
        AS->>AR: hasConflict(doctorId, date, time, null)
        AR->>DB: SELECT 1 FROM appointments WHERE doctor_id=? AND date=? AND time=? AND status='SCHEDULED'
        DB-->>AR: result row (or none)
        AR-->>AS: true / false

        alt conflict found
            AS-->>FC: throw ValidationException("doctor already has an appointment...")
            FC-->>U: Show inline error label, no DB write happens
        else no conflict
            AS->>AR: nextAppointmentCode()
            AR->>DB: SELECT COUNT(*) FROM appointments
            DB-->>AR: count
            AR-->>AS: "APT-00042"

            AS->>AR: save(appointment)
            AR->>DB: INSERT INTO appointments (...) VALUES (...)
            DB-->>AR: rows affected
            AR->>DB: SELECT last_insert_rowid()
            DB-->>AR: generated id
            AR-->>AS: Appointment (with id set)

            AS-->>FC: Appointment (booked)
            FC->>FC: onSaved.accept(appointment)
            FC-->>U: Close dialog, table refreshes
        end
    end
    deactivate AS
```

## Why the conflict check happens twice (service + schema)

1. **Service layer** (`AppointmentRepository.hasConflict`): the primary check — runs *before* any write, lets the app show a friendly `ValidationException` message instead of a raw SQL error.
2. **Schema constraint** (`UNIQUE (doctor_id, appointment_date, appointment_time)`): the backstop — protects data integrity even if two requests race past the first check (not possible in this single-connection desktop build, but this is the same pattern you'd want if the app were ever extended to a multi-client server).
