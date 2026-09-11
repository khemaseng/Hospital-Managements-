# UML Class Diagram

Rendered with [Mermaid](https://mermaid.js.org/) — view in GitHub, Obsidian, VS Code's Mermaid preview, or paste into https://mermaid.live.

```mermaid
classDiagram
    class Main {
        +start(Stage)
        +stop()
        +main(String[])
    }
    class Launcher {
        +main(String[])
    }

    class SceneManager {
        -Stage primaryStage
        -boolean darkMode
        +showLogin()
        +showDashboard(User)
        +toggleDarkMode()
    }

    class SessionManager {
        -User currentUser
        +login(User)
        +logout()
        +hasPermission(String) bool
    }

    class LoginController
    class MainLayoutController
    class DashboardController
    class PatientController
    class PatientFormController
    class DoctorController
    class DoctorFormController
    class AppointmentController
    class AppointmentFormController

    class AuthService {
        +login(String, String) User
        +logout()
        +register(...) User
    }
    class PatientService {
        +addPatient(Patient) Patient
        +updatePatient(Patient)
        +deletePatient(int)
        +search(...) List~Patient~
    }
    class DoctorService {
        +addDoctor(Doctor) Doctor
        +updateDoctor(Doctor)
        +deleteDoctor(int)
    }
    class AppointmentService {
        +bookAppointment(Appointment) Appointment
        +reschedule(...)
        +cancelAppointment(int)
    }
    class DashboardService {
        +getStats() DashboardStats
    }

    class UserRepository
    class PatientRepository
    class DoctorRepository
    class AppointmentRepository

    class DatabaseConnection {
        +getConnection() Connection$
        +initializeDatabase()$
        +lastInsertRowId(Connection) int$
    }

    class User {
        -int id
        -String username
        -String passwordHash
        -UserRole role
    }
    class Patient {
        -int id
        -String patientCode
        -String fullName
        -String gender
        -int age
        -LocalDate dateOfBirth
    }
    class Doctor {
        -int id
        -String doctorCode
        -String fullName
        -String department
        -double consultationFee
    }
    class Appointment {
        -int id
        -String appointmentCode
        -int patientId
        -int doctorId
        -LocalDate appointmentDate
        -LocalTime appointmentTime
        -AppointmentStatus status
    }
    class UserRole {
        <<enumeration>>
        ADMIN
        DOCTOR
        RECEPTIONIST
    }
    class AppointmentStatus {
        <<enumeration>>
        SCHEDULED
        COMPLETED
        CANCELLED
        NO_SHOW
    }

    class ValidationUtil
    class PasswordUtil
    class DialogUtil
    class ValidationException
    class DataAccessException

    Main --> SceneManager
    Main --> DatabaseConnection
    Launcher --> Main

    LoginController --> AuthService
    LoginController --> SceneManager
    MainLayoutController --> SceneManager
    MainLayoutController --> AuthService
    DashboardController --> DashboardService
    PatientController --> PatientService
    PatientController --> PatientFormController : opens
    PatientFormController --> PatientService
    DoctorController --> DoctorService
    DoctorController --> DoctorFormController : opens
    DoctorFormController --> DoctorService
    AppointmentController --> AppointmentService
    AppointmentController --> AppointmentFormController : opens
    AppointmentFormController --> AppointmentService
    AppointmentFormController --> PatientService
    AppointmentFormController --> DoctorService

    AuthService --> UserRepository
    AuthService --> SessionManager
    AuthService --> PasswordUtil
    PatientService --> PatientRepository
    PatientService --> ValidationUtil
    DoctorService --> DoctorRepository
    DoctorService --> ValidationUtil
    AppointmentService --> AppointmentRepository
    AppointmentService --> ValidationUtil
    DashboardService --> PatientService
    DashboardService --> DoctorService
    DashboardService --> AppointmentService

    UserRepository --> DatabaseConnection
    PatientRepository --> DatabaseConnection
    DoctorRepository --> DatabaseConnection
    AppointmentRepository --> DatabaseConnection

    UserRepository ..> User : maps rows to
    PatientRepository ..> Patient : maps rows to
    DoctorRepository ..> Doctor : maps rows to
    AppointmentRepository ..> Appointment : maps rows to

    User --> UserRole
    Appointment --> AppointmentStatus
    Appointment --> Patient : references
    Appointment --> Doctor : references

    PatientService ..> ValidationException : throws
    DoctorService ..> ValidationException : throws
    AppointmentService ..> ValidationException : throws
    UserRepository ..> DataAccessException : throws
    PatientRepository ..> DataAccessException : throws
```

## Design notes

- **MVC**: `controller/` = Controller, `model/` = Model, `fxml/` + `css/` = View.
- **Layering**: Controller → Service → Repository → DatabaseConnection. Controllers never touch JDBC directly; repositories never contain business rules (e.g. the double-booking check lives in `AppointmentService`, not `AppointmentRepository`).
- **Single Responsibility**: each repository handles exactly one table's CRUD; each service owns exactly one module's business rules; each controller wires exactly one FXML view.
- **Encapsulation**: all model fields are private with JavaFX property accessors (`xProperty()`) for TableView binding.
- **Polymorphism/Interfaces**: `UserRole` and `AppointmentStatus` enums drive `switch` expressions in `SessionManager.hasPermission()` and CSS badge styling, rather than scattering `if/else` chains.
