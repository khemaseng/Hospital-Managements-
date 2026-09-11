package com.hms.controller;

import com.hms.service.AppointmentService;
import com.hms.service.ExcelExportService;
import com.hms.service.ReportService;
import com.hms.util.DialogUtil;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class ReportsController {

    @FXML private ToggleButton dailyToggle;
    @FXML private ToggleButton weeklyToggle;
    @FXML private ToggleButton monthlyToggle;
    @FXML private ToggleButton yearlyToggle;
    @FXML private Label appointmentsInRangeLabel;
    @FXML private Label revenueInRangeLabel;
    @FXML private BarChart<String, Number> departmentChart;
    @FXML private PieChart statusChart;

    private final ReportService reportService = new ReportService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final ExcelExportService excelExportService = new ExcelExportService();

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    private void handleRangeChanged() {
        refresh();
    }

    private int selectedDays() {
        if (dailyToggle.isSelected()) return 1;
        if (weeklyToggle.isSelected()) return 7;
        if (yearlyToggle.isSelected()) return 365;
        return 30; // monthly (default)
    }

    private void refresh() {
        int days = selectedDays();
        appointmentsInRangeLabel.setText(String.valueOf(reportService.appointmentsInLastNDays(days)));
        revenueInRangeLabel.setText(String.format(Locale.US, "$%,.2f", reportService.revenueInLastNDays(days)));

        // Department bar chart
        departmentChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : reportService.appointmentsByDepartment().entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        departmentChart.getData().add(series);

        // Status pie chart
        statusChart.getData().clear();
        for (Map.Entry<String, Integer> entry : reportService.appointmentsByStatus().entrySet()) {
            statusChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Appointments to Excel");
        chooser.setInitialFileName("hms-appointments-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
        Stage stage = (Stage) departmentChart.getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try {
            excelExportService.exportAppointments(appointmentService.getAllAppointments(), file);
            DialogUtil.showInfo("Export Complete", "Appointments exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            DialogUtil.showError("Export Failed", "Could not write the Excel file: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Report Summary");
        chooser.setInitialFileName("hms-report-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text file", "*.txt"));
        Stage stage = (Stage) departmentChart.getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            int days = selectedDays();
            writer.println("HOSPITAL MANAGEMENT SYSTEM - REPORT SUMMARY");
            writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
            writer.println("Range: last " + days + " day(s)");
            writer.println("----------------------------------------");
            writer.println("Appointments in range: " + reportService.appointmentsInLastNDays(days));
            writer.println(String.format(Locale.US, "Revenue in range: $%,.2f", reportService.revenueInLastNDays(days)));
            writer.println();
            writer.println("Appointments by department:");
            reportService.appointmentsByDepartment().forEach((k, v) -> writer.println("  " + k + ": " + v));
            writer.println();
            writer.println("Appointments by status:");
            reportService.appointmentsByStatus().forEach((k, v) -> writer.println("  " + k + ": " + v));
            DialogUtil.showInfo("Export Complete", "Report summary saved to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            DialogUtil.showError("Export Failed", "Could not write the report file: " + e.getMessage());
        }
    }
}
