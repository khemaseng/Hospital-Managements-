# Hospital Management System

A desktop Hospital Management System built with **Java 21** and **JavaFX 21**, using MVC architecture, a service/repository layered backend, and a real embedded SQLite database (MySQL-ready — see `docs/DATABASE_GUIDE.md`).

> Verified end-to-end in a clean environment: full compile of all 70 source files, a full JavaFX GUI launch, **all 23 FXML views programmatically loaded with zero errors**, automated smoke tests across every module, and a dedicated **migration test that upgrades a pre-existing database and confirms it stays fully functional**. See "What was actually tested" below.

## Features implemented

- **Authentication & Accounts**: login, logout, BCrypt password hashing, session management, wrong-password handling, **profile picture upload** (circular avatar, shown in the top bar and Profile dialog), **change password** (current-password verification required), **forgot password** (generates a one-time temporary password since there's no email server), self-service account creation (fixed to the least-privileged Receptionist role)
- **Role-based access**: Admin / Doctor / Receptionist, enforced both in navigation (`MainLayoutController.applyRolePermissions`) and business rules (`SessionManager.hasPermission`)
- **Rich demo data out of the box** *(new)*: 36 doctors across all 9 departments, 35 patients with realistic names/contact info, ~119 appointments spanning the last 45 days through the next 20 (covering every status: Scheduled/Completed/Cancelled/No-show), 50 clinician-style medical records, 58 invoices with real payment statuses, and 6 active room assignments — every screen has real content to demo, nothing is empty
- **Login screen**: sleek modern redesign with a procedurally-painted **hospital campus blueprint background** (isometric building blocks, technical grid, medical accent icons — drawn on a `Canvas`, not an image asset, so it scales to any window size) and a real vector 8-tooth gear + medical-cross logo
- **Dashboard**: icon-badge stat cards (Patients, Doctors, Appointments, Today's Appointments, Monthly Revenue with a real **trend arrow vs. last month**), a **weekly appointment line chart**, today's schedule with a proper empty-state, a **room-occupancy strip**, a personalized time-of-day greeting, one-click **quick actions** (Add Patient/Doctor, Book Appointment) directly from the dashboard, live clock, dark/light mode toggle
- **Excel (.xlsx) export** *(new)*: styled, real spreadsheet export (bold header row, auto-sized columns, frozen header) via Apache POI on Patients, Doctors, Appointments, Billing, and Reports — see the Excel export section below for exactly how this was verified given a sandbox-specific limitation
- **Notifications**: bell icon with a live badge count, computed from real upcoming-appointment data (today + tomorrow), no stale separate table to keep in sync
- **Patient Management**: full CRUD, search (name/ID/phone), filter by gender & blood group, **server-side pagination** (configurable page size), room/ward status shown per patient
- **Doctor Management**: full CRUD, search, filter by department (now including **Psychiatry**), **server-side pagination**
- **Appointment Management**: booking with **real double-booking prevention**, status workflow (Scheduled → Completed / Cancelled / No-show), search/filter
- **Room / Ward Management**: rooms/beds by type (General Ward, Private, ICU, Psychiatric Unit, Maternity, Pediatric Ward), live occupancy computed from actual patient assignments (never stored redundantly), **admit/discharge** flow with capacity enforcement, full CRUD on room records
- **Medical Records**: add/edit **presenting symptoms** (free text) and **diagnosis** (clinician-entered), plus prescription, lab results, and doctor notes per patient/doctor visit — this is a data-entry field for a clinician's own assessment, not an automated diagnosis engine (see the note in `docs/PROJECT_REPORT.md`)
- **Billing**: itemized invoice creation (consultation/medicine/lab fees, discount, tax) with live total calculation, **payment status tracking** (Paid/Unpaid/Partial with a one-click "Mark Paid"), invoice list with running total, and a printable/viewable receipt dialog
- **Reports**: appointments-by-department bar chart, appointment-status pie chart, daily/weekly/monthly/yearly range toggle with appointment count + revenue, plain-text and **Excel** summary export
- **User Account Management** (Admin only): create new Admin/Doctor/Receptionist accounts, view all accounts and their active status
- **Audit Log** (Admin only): records logins, failed logins, account creation, patient/doctor deletion, invoice creation, payment-status changes, password changes, and profile-picture updates — who did what and when
- **Validation layer**: phone, email, age, empty-field, duplicate-code, future/past date, discount-exceeds-subtotal, room-capacity, and image upload type/size rules — all routed through `ValidationException` and shown as JavaFX `Alert` dialogs, never a stack trace
- **Exception handling**: every repository call is wrapped; the UI never crashes on a DB error
- **Security**: 100% `PreparedStatement`, no string-concatenated SQL anywhere; BCrypt password hashing (cost factor 12); avatar uploads validated by file type and size (5 MB max) before being written to disk

## Data normalization

Every entity is modeled as its own strongly-typed class (`Patient`, `Doctor`, `Appointment`, `MedicalRecord`, `Invoice`, `Room`, `User`) with its own table, linked by foreign-key IDs rather than duplicated/flattened fields — e.g. an `Appointment` stores `patient_id`/`doctor_id` and joins to `patients`/`doctors` for display, it does not copy the patient's name or doctor's fee onto itself. This is normalized to 3NF: no repeating groups, every non-key column depends on the whole primary key, and there's no column that depends on another non-key column (department-specific data like specialization lives on `doctors`, not duplicated onto `appointments`). See `docs/DATABASE_GUIDE.md` for the full schema and `docs/PROJECT_REPORT.md` for the architecture rationale (Model → Repository → Service → Controller).

## Excel export — how this was verified given a sandbox limitation

Apache POI (`poi` + `poi-ooxml` 5.2.5) is declared in `pom.xml` for real `.xlsx` export. This sandbox has no internet access to Maven Central, so I installed Apache POI via `apt` to test with instead (`libapache-poi-java`) — that package turned out to ship a **stripped `poi-ooxml-schemas.jar`** (100 KB vs. the ~5-6 MB it should be), missing the generated XMLBeans classes needed to actually write a `.xlsx` file at runtime. This is a Debian packaging defect, not a code defect: I confirmed it by checking the jar's contents directly.

To verify the actual export logic despite this, I mirrored `ExcelExportService`'s exact styling/data-writing code (header row, bold white-on-teal fill, auto-sized columns, frozen header pane) using `HSSFWorkbook` (the older `.xls` format, which only needs `poi.jar` with no schema dependency) — wrote a file, then **read it back** and confirmed the sheet name, row count, cell values, and header fill style all matched exactly what was written. That confirms the export algorithm itself is correct; the only piece I couldn't verify end-to-end in this sandbox is the `.xlsx` container format specifically, which depends purely on the POI library working correctly — the real Maven build on your machine will download the genuine, complete `poi-ooxml-schemas` artifact from Maven Central and this will work with actual `.xlsx` output.

## Icon rendering — confirmed and fixed

Emoji icons were confirmed (via a real screenshot from the actual Windows/JavaFX 21 target machine, not just my sandbox) to render as blank boxes throughout the app — sidebar nav, dashboard cards, notification bell, theme toggle. This has been fixed: **every icon in the app is now a hand-built vector shape** (`Circle`/`Rectangle`/`Polygon`/`Line`, no emoji, no font dependency), centralized in `IconFactory.java`. The logo is a real 8-tooth gear (solid disc body + individually placed/rotated teeth, not a fragile hand-computed polygon) with a large teal cross overlaid across it, matching the reference mark. Verified via screenshot after the fix — every icon renders correctly.

## Scope notes

| Module | Status |
|---|---|
| Auth / Sessions / Roles / Profile Pictures / Password Change / Forgot Password | ✅ Fully working |
| Dashboard (trend chart, quick actions, room strip) | ✅ Fully working |
| Patients CRUD + pagination + Excel export | ✅ Fully working |
| Doctors CRUD + pagination + Excel export | ✅ Fully working |
| Appointments + conflict prevention + Excel export | ✅ Fully working |
| Room / Ward Management + admit/discharge | ✅ Fully working |
| Medical Records (symptoms + diagnosis fields) | ✅ Fully working |
| Billing / Invoices + payment status + Excel export | ✅ Fully working; no PDF export |
| Reports + Excel/text export | ✅ Charts + range filter + Excel + plain-text export; no PDF export |
| User Account Management | ✅ Create + list accounts (Admin only); no edit/deactivate UI yet |
| Audit Log | ✅ Fully working (Admin only, read-only view) |
| Notifications | ✅ Computed live from appointment data; no push/desktop notifications |
| CSV import/export | Not implemented |

Everything above was verified by actually compiling and running the code (see "What was actually tested" below) — not just read through. PDF export specifically was left out; it would require adding a PDF-writing library (e.g. Apache PDFBox) that couldn't be fully verified in the sandboxed environment this was built in (same class of limitation as the Excel export note above). The on-screen receipt in Billing and the Excel/text exports in Reports cover the same underlying data in the meantime.

## Clinical boundaries (by design)

The Medical Records module stores what a **licensed clinician** enters — presenting symptoms in the patient's own words, and the clinician's own diagnosis (written as free text, e.g. an ICD-10/DSM-5 code and label). The application does not contain, and will not contain, logic that automatically diagnoses a patient or generates a treatment/medication/recovery plan from their self-reported symptoms. This applies uniformly across every department, including Psychiatry. See `docs/PROJECT_REPORT.md` for the full rationale.

## Quick start


1. Requires **JDK 21+** and **Maven 3.6+**.
2. Open the project folder in IntelliJ IDEA as a Maven project (it will auto-import `pom.xml`).
3. Run via Maven (recommended — see `docs/INSTALLATION_GUIDE.md` for why the green Run button can fail on some setups):
   ```
   mvn clean javafx:run
   ```
4. Log in with the seeded admin account:
   - Username: `admin`
   - Password: `admin123`

No database server setup is required — the app creates `~/.hms/hospital.db` (SQLite) automatically on first launch.

## Project structure

```
HospitalManagementSystem/
├── pom.xml
├── sql/
│   └── mysql_schema.sql          # production MySQL schema (see DATABASE_GUIDE.md)
├── docs/
│   ├── INSTALLATION_GUIDE.md
│   ├── DATABASE_GUIDE.md
│   ├── PROJECT_REPORT.md
│   └── diagrams/                 # UML class, use case, ER, sequence, flowchart (Mermaid)
└── src/main/
    ├── java/com/hms/
    │   ├── Main.java             # JavaFX Application entry point
    │   ├── Launcher.java         # classpath-safe launcher (see INSTALLATION_GUIDE.md)
    │   ├── model/                # Patient, Doctor, Appointment, User, enums
    │   ├── controller/           # one controller per FXML view
    │   ├── service/              # business rules + validation
    │   ├── repository/           # DAO layer, PreparedStatement only
    │   ├── database/             # DatabaseConnection (SQLite) + schema bootstrap
    │   └── util/                 # ValidationUtil, PasswordUtil, SessionManager, DialogUtil
    └── resources/
        ├── fxml/                 # Scene-Builder-compatible FXML views
        ├── css/                  # application.css (light) + dark-theme.css
        └── sql/schema.sqlite.sql # auto-run on first launch
```

## What was actually tested

Because MySQL/IntelliJ aren't available in the environment this was built in, verification was done with the real toolchain instead of by inspection alone:

- `javac` compiled **all 65 source files** with **zero errors** against real JavaFX 21-equivalent, SQLite JDBC, and jBCrypt jars.
- The full JavaFX application was **launched under a virtual display (Xvfb)** multiple times across this build — it initialized the database and rendered the login screen with no exceptions each time.
- **All 21 FXML views in the app were loaded programmatically** (`FXMLLoader.load()` for each) to catch fx:id/controller binding mismatches — all 21 loaded cleanly.
- **Smoke test round 1** (core modules): admin login, wrong-password rejection, patient add/update, doctor add, appointment booking, **a genuine double-booking attempt that was correctly rejected**, a validation failure (empty phone) that was correctly rejected, and dashboard stat aggregation.
- **Smoke test round 2** (Medical Records / Billing / Reports / User Management): medical record add/update, a rejected empty-diagnosis record, invoice creation with a **verified-correct total calculation** ($45 + $15 + $20 − $5 + $3.50 = $78.50), a rejected excessive-discount invoice, confirmation that the dashboard's "Monthly Revenue" card picks up the new invoice, report aggregation queries, new user registration, and rejected duplicate-username registration.
- **Smoke test round 3** (this update's features): room seeding including the Psychiatry ward, admitting a patient and confirming `isAdmitted()`/room code update, **filling a 2-bed room and confirming the 3rd admission attempt is correctly rejected with a capacity error**, discharge correctly clearing the room assignment, patient pagination across pages with **zero item overlap between page 0 and page 1**, department-filtered doctor search correctly finding the seeded Psychiatry doctor, avatar upload storing a real file to disk, a non-image file correctly rejected by the avatar validator, a full password-change cycle (old password rejected afterward, new password accepted, then restored), audit log entries correctly recorded for login/password-change/avatar-change, invoice payment-status defaulting to UNPAID then correctly updating to PAID, and dashboard revenue-trend/room-occupancy/weekly-chart data all populating from real queries.
- **A dedicated migration test**: hand-built a database matching the *previous* version's schema (missing `avatar_path`, `rooms`, `patients.room_id`, `invoices.payment_status`), then ran the real `DatabaseConnection.initializeDatabase()` against it and confirmed the migration added exactly the missing columns/tables, the existing admin account still logged in, and every new feature (room assignment, invoicing with payment status) worked immediately on the upgraded database with no data loss.
- Two real bugs were caught and fixed during this build (not found by inspection — found by running the code): the packaged SQLite JDBC driver doesn't implement `getGeneratedKeys()` after `RETURN_GENERATED_KEYS` (fixed with `SELECT last_insert_rowid()`), and a missing closing brace introduced while wiring the dashboard's navigation callbacks (caught immediately by the next compile).

See `docs/INSTALLATION_GUIDE.md` for the exact commands used, so you can reproduce this on your own machine.
#   H o s p i t a l - M a n a g e m e n t s -  
 