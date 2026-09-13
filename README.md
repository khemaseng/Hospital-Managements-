# 🏥 Hospital Management System (HMS)

A desktop Hospital Management System built with **Java 21** and **JavaFX 21**, following the **MVC architecture** with a service/repository layered backend and an embedded **SQLite database**.

> **Verified End-to-End:** Clean environment build across all source files, full JavaFX GUI launch, programmatically loaded FXML views, and automated database schema creation.

## Key Features

### Authentication & Accounts
* **Security:** Login, logout, BCrypt password hashing, session management, and failed-login detection.
* **Profile Management:** Dynamic visual profile avatar picture uploads.
* **Account Controls:** Password updates and one-time temporary password resets.
* **Self-Service Registration:** Account creation locked to standard Receptionist roles by default.

### Role-Based Access Control (RBAC)
* Dynamic UI access enforcement across **Admin**, **Doctor**, and **Receptionist** roles.
* Restricts navigation options (`MainLayoutController.applyRolePermissions`) and business execution (`SessionManager.hasPermission`).

### Dynamic Executive Dashboard
* **Metrics:** Real-time stat cards (Patients, Doctors, Appointments, Monthly Revenue).
* **Analytics:** Visual trend indicators versus previous month, weekly appointment line charts, and status pie charts.
* **Quick Actions:** Direct shortcuts to register patients, add doctors, or book appointments.

### Appointment Management
* Overlap and conflict prevention ensuring doctors cannot be double-booked at the same time block.
* Multi-status tracking (`SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`).

### Bed Management & Patient Care
* Ward capacity tracking preventing over-allocation when rooms reach full occupancy.
* Clinician-entered medical records capturing chief complaints, diagnosis notes, and treatments.

### Billing & Invoicing System
* Automated subtotal, discount, and tax calculation engine.
* Invoice status flows supporting `PAID`, `UNPAID`, and `PARTIAL` states.

### System Audit Logging
* Immutable event logging tracking sensitive system operations (Admin only).

### Data Exporting
* Built-in Apache POI integration to export formatted data tables directly into Microsoft Excel (`.xlsx`) spreadsheets.

## Technology Stack

* **Language:** Java 21 (JDK 21)
* **GUI Toolkit:** JavaFX 21 with FXML & CSS
* **Architecture:** Model-View-Controller (MVC) + Service/Repository Pattern
* **Database:** Embedded SQLite via JDBC
* **Security:** jBCrypt Password Hashing
* **Build System:** Apache Maven
* **Excel Engine:** Apache POI

## Getting Started

### Prerequisites
* **Java Development Kit (JDK 21)** or higher.
* **Apache Maven 3.8+** (or use IntelliJ's built-in Maven).

### Running the Application
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/khemaseng/Hospital-Managements.git](https://github.com/khemaseng/Hospital-Managements.git)
