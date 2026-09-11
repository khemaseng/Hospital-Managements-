# Database Guide

## Default: embedded SQLite (zero setup)

The app ships wired to SQLite so it runs immediately with no external server:

- Driver: `org.xerial:sqlite-jdbc`
- File location: `~/.hms/hospital.db` (created automatically on first launch)
- Schema source: `src/main/resources/sql/schema.sqlite.sql`, run automatically by `DatabaseConnection.initializeDatabase()` on every startup (all statements use `CREATE TABLE IF NOT EXISTS` / `INSERT OR IGNORE`, so re-running is safe and non-destructive)

All repository classes talk to the database only through `DatabaseConnection.getConnection()` — nothing outside `com.hms.database` knows or cares which database engine is behind that connection.

## Switching to MySQL

The project also ships a full MySQL-flavoured schema at `sql/mysql_schema.sql` (per the original spec: `CREATE DATABASE`, proper `ENGINE=InnoDB` tables, `FOREIGN KEY` constraints, indexes, an `ENUM` for roles/status). To switch:

### 1. Create the database

```bash
mysql -u root -p < sql/mysql_schema.sql
```

### 2. Add the MySQL Connector/J dependency to `pom.xml`

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>
```

(You can leave the `sqlite-jdbc` dependency in place or remove it — it's not required once you switch.)

### 3. Update `DatabaseConnection`

In `src/main/java/com/hms/database/DatabaseConnection.java`, replace the SQLite connection setup with:

```java
private static final String URL = "jdbc:mysql://localhost:3306/hospital_management?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your-password";

public static synchronized Connection getConnection() {
    try {
        if (connection == null || connection.isClosed()) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    } catch (SQLException | ClassNotFoundException e) {
        throw new DataAccessException("Unable to connect to the database.", e);
    }
}
```

Also remove or skip the `initializeDatabase()` call in `Main.start()` — with MySQL you run `mysql_schema.sql` once via the CLI instead of auto-running a schema script on every launch (or adapt `initializeDatabase()` to load `mysql_schema.sql` from `sql/` if you want the same auto-bootstrap behavior).

### 4. That's it

Because every repository (`PatientRepository`, `DoctorRepository`, `AppointmentRepository`, `UserRepository`) uses only `PreparedStatement` with standard SQL (no SQLite-specific syntax except `datetime('now')` / `date('now')` defaults, which MySQL's schema replaces with `CURRENT_TIMESTAMP` / `CURRENT_DATE`), **no repository, service, or controller code needs to change**. This is the payoff of the DAO/Repository pattern: the persistence engine is swappable behind one class.

### One caveat: `last_insert_rowid()`

`DatabaseConnection.lastInsertRowId(Connection)` currently runs SQLite's `SELECT last_insert_rowid()`. On MySQL, replace its body with:

```java
public static int lastInsertRowId(Connection conn) throws SQLException {
    try (Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()")) {
        return rs.next() ? rs.getInt(1) : -1;
    }
}
```

(This helper exists because the bundled SQLite JDBC driver doesn't implement `PreparedStatement.getGeneratedKeys()` after `Statement.RETURN_GENERATED_KEYS` — see the root `README.md` for details. MySQL Connector/J *does* support `getGeneratedKeys()` properly, so switching back to that pattern on MySQL is also an option if you prefer.)

## Schema overview

| Table | Purpose | Key relationships |
|---|---|---|
| `users` | Login accounts (Admin/Doctor/Receptionist) | referenced optionally by `doctors.user_id` |
| `patients` | Patient records | — |
| `doctors` | Doctor records | — |
| `appointments` | Booked slots | `patient_id` → patients, `doctor_id` → doctors; `UNIQUE(doctor_id, appointment_date, appointment_time)` enforces no double-booking at the DB level as a backstop to the service-layer check |
| `medical_records` | Diagnosis/prescription/notes per visit | `patient_id`, `doctor_id`, optional `appointment_id` |
| `invoices` | Billing line items | `patient_id`, optional `appointment_id` |

`medical_records` and `invoices` exist in both schemas and are read by the Dashboard's revenue calculation, but there's no CRUD UI for them yet in this build (see the "Deliberately reduced scope" section of the root `README.md`) — the tables and FK relationships are ready for that UI to be added.
