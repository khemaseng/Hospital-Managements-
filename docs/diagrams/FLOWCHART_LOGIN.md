# Flowchart: Login & Startup Flow

```mermaid
flowchart TD
    Start([App starts]) --> InitDB[DatabaseConnection.initializeDatabase]
    InitDB --> RunSchema[Run schema.sqlite.sql<br/>CREATE TABLE IF NOT EXISTS + seed data]
    RunSchema --> ShowLogin[SceneManager.showLogin]
    ShowLogin --> Wait{User enters<br/>username/password}

    Wait --> Submit[Click Sign In]
    Submit --> ValidateInput{Username & password<br/>non-empty?}
    ValidateInput -- No --> ShowError1[Show error:<br/>field cannot be empty]
    ShowError1 --> Wait

    ValidateInput -- Yes --> LookupUser[UserRepository.findByUsername]
    LookupUser --> UserExists{User found<br/>AND active?}
    UserExists -- No --> ShowError2[Show generic error:<br/>Invalid username or password]
    ShowError2 --> Wait

    UserExists -- Yes --> CheckPassword[PasswordUtil.verify<br/>BCrypt.checkpw]
    CheckPassword --> PasswordOK{Password<br/>matches hash?}
    PasswordOK -- No --> ShowError2

    PasswordOK -- Yes --> UpdateLastLogin[UserRepository.updateLastLogin]
    UpdateLastLogin --> CreateSession[SessionManager.login user]
    CreateSession --> ShowDashboard[SceneManager.showDashboard]
    ShowDashboard --> ApplyRole[MainLayoutController.applyRolePermissions<br/>filters sidebar by role]
    ApplyRole --> LoadDashboardView[Load DashboardView.fxml<br/>into content area]
    LoadDashboardView --> End([User is in the app])
```

## Notes

- The "generic error" branch for both *user not found* and *wrong password* is deliberate: it prevents username enumeration (an attacker can't tell whether a username exists by observing different error messages).
- Every step after `Submit` that touches the database is wrapped in a try/catch in `LoginController.handleLogin()`; any unexpected exception falls through to a generic "Unable to sign in right now" message rather than crashing the UI or leaking a stack trace to the user.
