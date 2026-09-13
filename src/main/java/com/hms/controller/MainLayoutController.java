package com.hms.controller;

import com.hms.model.User;
import com.hms.model.UserRole;
import com.hms.service.AuthService;
import com.hms.service.NotificationService;
import com.hms.util.DialogUtil;
import com.hms.util.IconFactory;
import com.hms.util.ImageUtil;
import com.hms.util.SessionManager;
import com.hms.view.NavigationBus;
import com.hms.view.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Controller for the application shell: sidebar navigation, top bar (clock,
 * notifications, dark mode toggle, profile avatar), and swapping the center
 * content pane between feature views.
 */
public class MainLayoutController {

    @FXML private StackPane contentArea;
  //  @FXML private StackPane sidebarLogoContainer;
    @FXML private StackPane searchIconContainer;
    @FXML private Label pageTitleLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private ImageView avatarImageView;
    @FXML private TextField globalSearchField;
    @FXML private Button themeToggleButton;
    @FXML private Button notificationButton;
    @FXML private Label notificationBadge;
    @FXML private Button navDashboard;
    @FXML private Button navPatients;
    @FXML private Button navDoctors;
    @FXML private Button navAppointments;
    @FXML private Button navRooms;
    @FXML private Button navMedicalRecords;
    @FXML private Button navBilling;
    @FXML private Button navReports;
    @FXML private Button navUsers;
    @FXML private Button navAuditLog;
    @FXML private Button navLogout;

    private final AuthService authService = new AuthService();
    private final NotificationService notificationService = new NotificationService();
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy | hh:mm a");
    private static final double NAV_ICON_SIZE = 16;

    @FXML
    public void initialize() {
        buildIcons();
        ImageUtil.makeCircular(avatarImageView, 18);
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null) {
            userNameLabel.setText(current.getFullName());
            userRoleLabel.setText(current.getRole().name());
            refreshAvatar();
        }
        applyRolePermissions();
        startClock();
        refreshNotificationBadge();
        registerNavigationBus();
        showDashboard();
    }

    /**
     * Every icon in the app shell is a vector shape from IconFactory, not
     * emoji - emoji glyphs were confirmed (via screenshots on the actual
     * target Windows machine) to render as blank boxes in JavaFX.
     */
    private void buildIcons() {
//        sidebarLogoContainer.getChildren().add(IconFactory.gearCrossLogo(56, true));
        searchIconContainer.getChildren().add(IconFactory.search(14, "icon-shape-dark"));
        notificationButton.setGraphic(IconFactory.bell(15, "icon-shape-dark"));
        themeToggleButton.setGraphic(IconFactory.moon(15, "icon-shape-dark"));

        navDashboard.setGraphic(IconFactory.grid(NAV_ICON_SIZE, "icon-shape-light"));
        navPatients.setGraphic(IconFactory.person(NAV_ICON_SIZE, "icon-shape-light"));
        navDoctors.setGraphic(IconFactory.doctorPerson(NAV_ICON_SIZE, "icon-shape-light", "icon-shape-light"));
        navAppointments.setGraphic(IconFactory.calendar(NAV_ICON_SIZE, "icon-shape-light"));
        navRooms.setGraphic(IconFactory.bed(NAV_ICON_SIZE, "icon-shape-light"));
        navMedicalRecords.setGraphic(IconFactory.clipboard(NAV_ICON_SIZE, "icon-shape-light"));
        navBilling.setGraphic(IconFactory.cash(NAV_ICON_SIZE, "icon-shape-light"));
        navReports.setGraphic(IconFactory.barChart(NAV_ICON_SIZE, "icon-shape-light"));
        navUsers.setGraphic(IconFactory.person(NAV_ICON_SIZE, "icon-shape-light"));
        navAuditLog.setGraphic(IconFactory.shield(NAV_ICON_SIZE, "icon-shape-light"));
        navLogout.setGraphic(IconFactory.logout(NAV_ICON_SIZE, "icon-shape-light"));
    }

    private void registerNavigationBus() {
        NavigationBus.setHandler(route -> {
            switch (route) {
                case "DASHBOARD" -> showDashboard();
                case "PATIENTS" -> showPatients();
                case "DOCTORS" -> showDoctors();
                case "APPOINTMENTS" -> showAppointments();
                case "ROOMS" -> showRooms();
                case "MEDICAL_RECORDS" -> showMedicalRecords();
                case "BILLING" -> showBilling();
                case "REPORTS" -> showReports();
                default -> { /* unknown route - no-op */ }
            }
        });
    }

    private void refreshAvatar() {
        User current = SessionManager.getInstance().getCurrentUser();
        Image image = current != null ? ImageUtil.loadAvatar(current.getAvatarPath()) : null;
        if (image != null) {
            avatarImageView.setImage(image);
        } else {
            // No photo uploaded yet - show a neutral vector placeholder instead of a blank frame.
            avatarImageView.setImage(null);
        }
    }

    private void refreshNotificationBadge() {
        int count = notificationService.getUpcomingCount();
        notificationBadge.setText(String.valueOf(count));
        notificationBadge.setVisible(count > 0);
        notificationBadge.setManaged(count > 0);
    }

    private void applyRolePermissions() {
        User current = current();
        boolean isAdmin = current.getRole() == UserRole.ADMIN;
        boolean isDoctor = current.getRole() == UserRole.DOCTOR;
        boolean isReceptionist = current.getRole() == UserRole.RECEPTIONIST;

        setVisibility(navDoctors, isAdmin || isReceptionist);
        setVisibility(navRooms, isAdmin || isReceptionist);
        setVisibility(navMedicalRecords, isAdmin || isDoctor);
        setVisibility(navBilling, isAdmin || isReceptionist);
        setVisibility(navReports, isAdmin);
        setVisibility(navUsers, isAdmin);
        setVisibility(navAuditLog, isAdmin);
    }

    private void setVisibility(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private User current() {
        return SessionManager.getInstance().getCurrentUser();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                dateTimeLabel.setText(java.time.LocalDateTime.now().format(DATETIME_FORMAT))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    @FXML
    private void showDashboard() {
        setActive(navDashboard);
        load("/fxml/DashboardView.fxml", "Dashboard");
    }

    @FXML
    private void showPatients() {
        setActive(navPatients);
        load("/fxml/PatientView.fxml", "Patients");
    }

    @FXML
    private void showDoctors() {
        setActive(navDoctors);
        load("/fxml/DoctorView.fxml", "Doctors");
    }

    @FXML
    private void showAppointments() {
        setActive(navAppointments);
        load("/fxml/AppointmentView.fxml", "Appointments");
    }

    @FXML
    private void showRooms() {
        setActive(navRooms);
        load("/fxml/RoomView.fxml", "Rooms / Wards");
    }

    @FXML
    private void showMedicalRecords() {
        setActive(navMedicalRecords);
        load("/fxml/MedicalRecordView.fxml", "Medical Records");
    }

    @FXML
    private void showBilling() {
        setActive(navBilling);
        load("/fxml/BillingView.fxml", "Billing");
    }

    @FXML
    private void showReports() {
        setActive(navReports);
        load("/fxml/ReportsView.fxml", "Reports");
    }

    @FXML
    private void showUserManagement() {
        setActive(navUsers);
        load("/fxml/UserManagementView.fxml", "User Accounts");
    }

    @FXML
    private void showAuditLog() {
        setActive(navAuditLog);
        load("/fxml/AuditLogView.fxml", "Audit Log");
    }

    @FXML
    private void toggleDarkMode() {
        SceneManager.getInstance().toggleDarkMode();
        boolean dark = SceneManager.getInstance().isDarkMode();
        themeToggleButton.setGraphic(dark
                ? IconFactory.sun(15, "icon-shape-dark")
                : IconFactory.moon(15, "icon-shape-dark"));
    }

    @FXML
    private void handleNotificationsClicked() {
        List<NotificationService.Notification> notifications = notificationService.getUpcomingNotifications();
        VBox content = new VBox(10);
        content.setPrefWidth(340);
        if (notifications.isEmpty()) {
            Label empty = new Label("No upcoming appointments in the next 24 hours.");
            empty.getStyleClass().add("muted");
            content.getChildren().add(empty);
        } else {
            for (NotificationService.Notification n : notifications) {
                Label title = new Label(n.title());
                title.setStyle("-fx-font-weight: bold;");
                Label message = new Label(n.message());
                message.getStyleClass().add("muted");
                VBox row = new VBox(2, title, message);
                content.getChildren().add(row);
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("Upcoming Appointments");
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    @FXML
    private void handleProfileClicked() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/ProfileDialog.fxml", "My Profile");
        ProfileController controller = handle.controller();
        controller.setOnAvatarChanged(this::refreshAvatar);
        handle.showAndWait();
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        SceneManager.getInstance().showLogin();
    }

    private void setActive(Button active) {
        for (Button b : new Button[]{navDashboard, navPatients, navDoctors, navAppointments, navRooms,
                navMedicalRecords, navBilling, navReports, navUsers, navAuditLog}) {
            b.getStyleClass().remove("sidebar-button-active");
        }
        active.getStyleClass().add("sidebar-button-active");
    }

    private void load(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxml)));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            pageTitleLabel.setText(title);
            refreshNotificationBadge();
        } catch (Exception e) {
            Label errorLabel = new Label("Unable to load this section: " + e.getMessage());
            errorLabel.getStyleClass().add("error-text");
            contentArea.getChildren().setAll(errorLabel);
        }
    }
}
