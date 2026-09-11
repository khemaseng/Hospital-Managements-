# Use Case Diagram

```mermaid
flowchart LR
    Admin([Admin])
    Doctor([Doctor])
    Receptionist([Receptionist])

    subgraph HMS [Hospital Management System]
        UC1[Login / Logout]
        UC2[View Dashboard]
        UC3[Manage Patients<br/>Add / Edit / Delete / Search]
        UC4[Manage Doctors<br/>Add / Edit / Delete / Search]
        UC5[Book Appointment]
        UC6[Update Appointment Status<br/>Complete / Cancel / No-show]
        UC7[Toggle Dark Mode]
        UC8[Manage User Accounts]
        UC9[View Reports]
    end

    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Admin --> UC4
    Admin --> UC5
    Admin --> UC6
    Admin --> UC7
    Admin --> UC8
    Admin --> UC9

    Doctor --> UC1
    Doctor --> UC2
    Doctor --> UC3
    Doctor --> UC5
    Doctor --> UC6
    Doctor --> UC7

    Receptionist --> UC1
    Receptionist --> UC2
    Receptionist --> UC3
    Receptionist --> UC4
    Receptionist --> UC5
    Receptionist --> UC6
    Receptionist --> UC7

    UC5 -.include.-> UC10[Check Doctor Availability<br/>conflict prevention]
```

## Notes on role permissions (enforced in `SessionManager.hasPermission`)

- **Admin**: unrestricted access to every module, including user account management and reports (both are admin-only in the permission matrix).
- **Doctor / Receptionist**: dashboard, patients, and appointments. Doctor/Receptionist-specific restrictions (billing for receptionists, medical records for doctors) are defined in the permission matrix and ready to enforce once those modules' UIs are built.
- User account management and reports are gated to Admin only; this build's `MainLayoutController` currently shows Dashboard/Patients/Doctors/Appointments to all logged-in roles per the simplified module set delivered (see root `README.md`), with the permission matrix in `SessionManager` already in place for when Reports/User Management UI is added.
