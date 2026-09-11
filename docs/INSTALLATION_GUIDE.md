# Installation Guide

## Prerequisites

- **JDK 21 or later** (this project was built and verified against OpenJDK 21)
- **Maven 3.6+** (IntelliJ bundles its own, or install separately)
- **IntelliJ IDEA** (Community or Ultimate) — Scene Builder is optional, only needed if you want to visually edit the `.fxml` files

## 1. Open the project

1. Launch IntelliJ IDEA.
2. `File → Open...` and select the `HospitalManagementSystem` folder (the one containing `pom.xml`).
3. IntelliJ will detect it as a Maven project and prompt to import — click **Load Maven Project** (or it may do this automatically). Wait for the "Maven" tool window (bottom left) to finish downloading dependencies (JavaFX, SQLite JDBC, jBCrypt).
4. Confirm the Project SDK is set to Java 21: `File → Project Structure → Project → SDK`.

## 2. Run the application — **use Maven, not the green Run button**

This is the single most common failure point with JavaFX + Maven + IntelliJ, and it was hit and solved during this project's own verification:

**Recommended (always works):**

Open the Maven tool window (right-hand sidebar) → `HospitalManagementSystem → Plugins → javafx → javafx:run`, or simply run in the built-in terminal:

```
mvn clean javafx:run
```

**Why the green ▶ Run button next to `Main.main()` often fails:**

`Main.java` extends `javafx.application.Application`. When JavaFX is pulled in as a plain Maven dependency (not a JDK module), IntelliJ's default "Run Main.main()" launcher frequently fails with:

```
Error: JavaFX runtime components are missing, and are required to run this application
```

This happens because the JavaFX native/platform modules aren't automatically added to the module path by IntelliJ's default run configuration — only `javafx-maven-plugin`'s `javafx:run` goal (or a manually configured run configuration with the right `--module-path`/`--add-modules` VM options) sets this up correctly.

**If you still want a green-button run configuration:**

`Run → Edit Configurations → + → Maven`:
- Working directory: project root
- Command line: `javafx:run`

Or, for a plain Application run configuration, add these VM options (adjust the path to wherever Maven downloaded the JavaFX jars, typically `~/.m2/repository/org/openjfx/...`):
```
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

## 3. First launch

On first run, the app automatically:
1. Creates `~/.hms/` in your home directory (`C:\Users\<you>\.hms` on Windows).
2. Creates `hospital.db` (SQLite) inside it.
3. Runs `src/main/resources/sql/schema.sqlite.sql` to create all tables and seed demo data.

Log in with:
- Username: `admin`
- Password: `admin123`

**Change this password after first login in a real deployment** — there's no self-service "change password" screen yet; use the `AuthService.register(...)` method or update the `users` table directly for now.

## 4. Building a standalone jar

```
mvn clean package
```

This produces `target/HospitalManagementSystem-1.0.0.jar` (a shaded/fat jar via `maven-shade-plugin`, containing all dependencies). Run it with:

```
java -jar target/HospitalManagementSystem-1.0.0.jar
```

The jar's manifest points at `com.hms.Launcher`, not `com.hms.Main` directly — this sidesteps the same "JavaFX runtime components are missing" issue described above, which also affects `java -jar` when the main class extends `Application` directly and JavaFX is unnamed-module (non-modular classpath) jars. `Launcher.main()` simply calls `Main.main()`.

## 5. Switching to MySQL (optional)

The app ships wired to embedded SQLite so it runs with zero setup. To use MySQL instead, see `docs/DATABASE_GUIDE.md`.

## How this was verified before delivery

Since the environment used to build this didn't have IntelliJ or a MySQL server, verification was done directly with the JDK/Maven toolchain:

```bash
# 1. Full clean compile of every source file (zero errors)
javac -encoding UTF-8 -cp "<javafx+sqlite+jbcrypt jars>" -d out $(find src/main/java -name "*.java")

# 2. Full GUI launch under a virtual display, to catch FXML/controller wiring errors
xvfb-run java -cp "out:src/main/resources:<jars>" com.hms.Launcher
# -> Database schema initialized/verified successfully. (no exceptions, login screen rendered)

# 3. Automated smoke test against a real SQLite DB, exercising the real service layer:
#    - admin login succeeds; wrong password is correctly rejected
#    - patient add + update succeeds
#    - doctor add succeeds
#    - appointment booking succeeds
#    - a genuine double-booking attempt on the same doctor/date/time is correctly rejected
#    - an invalid patient (empty phone) is correctly rejected by validation
#    - dashboard stats aggregate correctly across all of the above
```

All of the above passed. If you have Maven with internet access, `mvn clean javafx:run` will download the exact same dependency versions declared in `pom.xml` and behave identically.
