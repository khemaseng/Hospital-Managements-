
package com.hms.controller;

import com.hms.model.Appointment;
import com.hms.model.RecentRegistration;
import com.hms.service.AppointmentService;
import com.hms.service.DashboardService;
import com.hms.util.DialogUtil;
import com.hms.util.IconFactory;
import com.hms.view.NavigationBus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label greetingLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label monthlyRevenueLabel;
    @FXML private Label revenueTrendLabel;
    @FXML private Label roomOccupancyLabel;
    @FXML private VBox todayScheduleBox;

    @FXML private StackPane badgePatients;
    @FXML private StackPane badgeDoctors;
    @FXML private StackPane badgeAppointments;
    @FXML private StackPane badgeToday;
    @FXML private StackPane badgeRevenue;
    @FXML private StackPane watermarkPatients;
    @FXML private StackPane watermarkDoctors;
    @FXML private StackPane watermarkAppointments;
    @FXML private StackPane watermarkToday;
    @FXML private StackPane watermarkRevenue;
    @FXML private StackPane roomIconContainer;
    @FXML private StackPane recentSearchIconContainer;

    @FXML private TextField recentSearchField;
    @FXML private ToggleButton showAllToggle;
    @FXML private ToggleButton showPatientsToggle;
    @FXML private ToggleButton showDoctorsToggle;
    @FXML private TableView<RecentRegistration> recentTable;
    @FXML private TableColumn<RecentRegistration, String> colType;
    @FXML private TableColumn<RecentRegistration, String> colName;
    @FXML private TableColumn<RecentRegistration, String> colCode;
    @FXML private TableColumn<RecentRegistration, String> colAdded;

    private final DashboardService dashboardService = new DashboardService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final ObservableList<RecentRegistration> recentRegistrations = FXCollections.observableArrayList();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    @FXML
    public void initialize() {
        buildIcons();

        // Sets the clean dark-gray credit label instead of the default greeting
        if (greetingLabel != null) {
            greetingLabel.setText("Built by Seng SilKhema");
            greetingLabel.getStyleClass().setAll("built-by-heading");
        }

        DashboardService.DashboardStats stats = dashboardService.getStats();
        totalPatientsLabel.setText(String.valueOf(stats.totalPatients()));
        totalDoctorsLabel.setText(String.valueOf(stats.totalDoctors()));
        totalAppointmentsLabel.setText(String.valueOf(stats.totalAppointments()));
        todayAppointmentsLabel.setText(String.valueOf(stats.todayAppointments()));
        monthlyRevenueLabel.setText(String.format(Locale.US, "$%,.2f", stats.monthlyRevenue()));
        roomOccupancyLabel.setText(stats.occupiedBeds() + " / " + stats.totalBeds() + " beds occupied");

        double trend = stats.revenueTrendPercent();
        String arrow = trend >= 0 ? "▲" : "▼";
        String color = trend >= 0 ? "-fx-text-fill: #15803d;" : "-fx-text-fill: #b91c1c;";
        revenueTrendLabel.setText(arrow + " " + String.format(Locale.US, "%.0f%%", Math.abs(trend)) + " vs last month");
        revenueTrendLabel.setStyle(color + " -fx-font-size: 11px;");

        populateTodaySchedule();
        setupRecentRegistrationsTable();
        refreshRecentRegistrations();
    }

    private void buildIcons() {
        // Clear badge and watermark containers so the cards stay clean without icon clutter
        clearContainers(badgePatients, badgeDoctors, badgeAppointments, badgeToday, badgeRevenue);
        clearContainers(watermarkPatients, watermarkDoctors, watermarkAppointments, watermarkToday, watermarkRevenue);

        if (roomIconContainer != null) {
            roomIconContainer.getChildren().setAll(IconFactory.bed(24, "icon-shape-dark"));
        }
        if (recentSearchIconContainer != null) {
            recentSearchIconContainer.getChildren().setAll(IconFactory.search(14, "icon-shape-dark"));
        }
    }

    private void clearContainers(StackPane... panes) {
        for (StackPane pane : panes) {
            if (pane != null) {
                pane.getChildren().clear();
                pane.setVisible(false);
                pane.setManaged(false);
            }
        }
    }

    private void populateTodaySchedule() {
        List<Appointment> today = appointmentService.getAppointmentsForDate(LocalDate.now());
        todayScheduleBox.getChildren().clear();
        if (today.isEmpty()) {
            VBox emptyState = new VBox(6);
            emptyState.setStyle("-fx-alignment: center; -fx-padding: 30 0 30 0;");
            StackPane iconBox = new StackPane(IconFactory.calendar(34, "icon-shape-dark"));
            Label title = new Label("No appointments scheduled for today.");
            title.setStyle("-fx-font-weight: bold;");
            Label subtitle = new Label("New bookings will appear here.");
            subtitle.getStyleClass().add("muted");
            emptyState.getChildren().addAll(iconBox, title, subtitle);
            todayScheduleBox.getChildren().add(emptyState);
            return;
        }
        for (Appointment a : today) {
            HBox row = new HBox(14);
            row.setStyle("-fx-alignment: center-left;");
            Label time = new Label(a.getAppointmentTime().format(TIME_FORMAT));
            time.setStyle("-fx-font-weight: bold; -fx-min-width: 78;");
            VBox details = new VBox(1);
            Label name = new Label(a.getPatientName() + " → Dr. " + a.getDoctorName());
            Label dept = new Label(a.getDepartment());
            dept.getStyleClass().add("muted");
            dept.setStyle("-fx-font-size: 11px;");
            details.getChildren().addAll(name, dept);
            Label status = new Label(a.getStatus().name());
            status.getStyleClass().addAll("badge", badgeClass(a.getStatus().name()));
            row.getChildren().addAll(time, details, status);
            todayScheduleBox.getChildren().add(row);
        }
    }

    private String badgeClass(String status) {
        return switch (status) {
            case "SCHEDULED" -> "badge-scheduled";
            case "COMPLETED" -> "badge-completed";
            case "CANCELLED" -> "badge-cancelled";
            default -> "badge-noshow";
        };
    }

    private void setupRecentRegistrationsTable() {
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().type()));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(type);
                badge.getStyleClass().addAll("badge", type.equals("Doctor") ? "badge-completed" : "badge-scheduled");
                setAlignment(Pos.CENTER);
                setGraphic(badge);
            }
        });
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name()));
        colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().code()));
        colAdded.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().addedOn()));
        recentTable.setItems(recentRegistrations);
        recentTable.setPlaceholder(new Label("No recent registrations to show."));
    }

    @FXML
    private void handleRecentFilterChanged() {
        refreshRecentRegistrations();
    }

    private void refreshRecentRegistrations() {
        List<RecentRegistration> all = dashboardService.getRecentRegistrations(10);
        String keyword = recentSearchField != null ? recentSearchField.getText() : "";
        boolean patientsOnly = showPatientsToggle != null && showPatientsToggle.isSelected();
        boolean doctorsOnly = showDoctorsToggle != null && showDoctorsToggle.isSelected();

        List<RecentRegistration> filtered = all.stream()
                .filter(r -> !patientsOnly || r.type().equals("Patient"))
                .filter(r -> !doctorsOnly || r.type().equals("Doctor"))
                .filter(r -> keyword == null || keyword.isBlank()
                        || r.name().toLowerCase().contains(keyword.toLowerCase())
                        || r.code().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
        recentRegistrations.setAll(filtered);
    }

    @FXML
    private void handleQuickAddPatient() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/PatientFormDialog.fxml", "Add Patient");
        PatientFormController controller = handle.controller();
        controller.setOnSaved(p -> initialize());
        handle.showAndWait();
    }

    @FXML
    private void handleQuickAddDoctor() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/DoctorFormDialog.fxml", "Add Doctor");
        DoctorFormController controller = handle.controller();
        controller.setOnSaved(d -> initialize());
        handle.showAndWait();
    }

    @FXML
    private void handleQuickBookAppointment() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/AppointmentFormDialog.fxml", "Book Appointment");
        AppointmentFormController controller = handle.controller();
        controller.setOnSaved(a -> initialize());
        handle.showAndWait();
    }

    @FXML
    private void handleViewAllAppointments() {
        NavigationBus.navigateTo("APPOINTMENTS");
    }

    @FXML
    private void handleManageRooms() {
        NavigationBus.navigateTo("ROOMS");
    }
}