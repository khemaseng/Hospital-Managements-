
package com.hms.controller;

import com.hms.model.Patient;
import com.hms.service.ExcelExportService;
import com.hms.service.PatientService;
import com.hms.util.DialogUtil;
import com.hms.util.IconFactory;
import com.hms.util.Page;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PatientController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> genderFilter;
    @FXML private ComboBox<String> bloodGroupFilter;
    @FXML private ComboBox<String> sortFilter; // Dropdown សម្រាប់ Sorting
    @FXML private Label resultCountLabel;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> colCode;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, String> colGender;
    @FXML private TableColumn<Patient, Number> colAge;
    @FXML private TableColumn<Patient, String> colPhone;
    @FXML private TableColumn<Patient, String> colBloodGroup;
    @FXML private TableColumn<Patient, String> colRoom;
    @FXML private TableColumn<Patient, Void> colActions;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private ComboBox<Integer> pageSizeCombo;

    private final PatientService patientService = new PatientService();
    private final ExcelExportService excelExportService = new ExcelExportService();
    private final ObservableList<Patient> patients = FXCollections.observableArrayList();

    private int currentPage = 0;
    private int pageSize = 8;

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("patientCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colBloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        colRoom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().isAdmitted() ? data.getValue().getRoomCode() : "-"));
        addActionButtons();

        if (prevPageButton != null) {
            prevPageButton.setGraphic(IconFactory.chevronLeft(11, "icon-shape-dark"));
        }
        if (nextPageButton != null) {
            nextPageButton.setGraphic(IconFactory.chevronRight(11, "icon-shape-dark"));
            nextPageButton.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        }

        // Initialize Filter Items
        genderFilter.getItems().setAll("All", "Male", "Female", "Other");
        genderFilter.setValue("All");

        bloodGroupFilter.getItems().setAll("All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodGroupFilter.setValue("All");

        // Initialize Sort Items (ប្រសិនបើ FXML មាន ComboBox sortFilter)
        if (sortFilter != null) {
            sortFilter.getItems().setAll(
                    "Name (A-Z)",
                    "Name (Z-A)",
                    "Age (Youngest)",
                    "Age (Oldest)",
                    "Patient ID"
            );
            sortFilter.setValue("Name (A-Z)");
            sortFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentPage = 0;
                refresh();
            });
        }

        if (pageSizeCombo != null) {
            pageSizeCombo.getItems().setAll(8, 15, 25, 50);
            pageSizeCombo.setValue(pageSize);
            pageSizeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    pageSize = newVal;
                    currentPage = 0;
                    refresh();
                }
            });
        }

        // ចង Listener ដោយផ្ទាល់ - User រើស Filter ឬវាយ Search ភ្លាម រត់ Refresh ភ្លាម
        genderFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            refresh();
        });

        bloodGroupFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            refresh();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            refresh();
        });

        patientTable.setItems(patients);
        refresh();
    }

    private void refresh() {
        String keyword = searchField != null ? searchField.getText() : "";
        String gender = genderFilter != null && genderFilter.getValue() != null ? genderFilter.getValue() : "All";
        String blood = bloodGroupFilter != null && bloodGroupFilter.getValue() != null ? bloodGroupFilter.getValue() : "All";
        String sortBy = sortFilter != null && sortFilter.getValue() != null ? sortFilter.getValue() : "Name (A-Z)";

        Page<Patient> page = patientService.search(keyword, gender, blood, sortBy, currentPage, pageSize);
        patients.setAll(page.items());

        if (resultCountLabel != null) {
            resultCountLabel.setText(page.totalItems() + " patient(s)");
        }
        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Page " + (page.pageIndex() + 1) + " of " + Math.max(1, page.totalPages()));
        }
        if (prevPageButton != null) {
            prevPageButton.setDisable(!page.hasPrevious());
        }
        if (nextPageButton != null) {
            nextPageButton.setDisable(!page.hasNext());
        }
    }

    @FXML
    private void handleFilterChanged(KeyEvent event) {
        currentPage = 0;
        refresh();
    }

    @FXML
    private void handleFilterChanged() {
        currentPage = 0;
        refresh();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            refresh();
        }
    }

    @FXML
    private void handleNextPage() {
        currentPage++;
        refresh();
    }

    @FXML
    private void handlePageSizeChanged() {
        if (pageSizeCombo != null && pageSizeCombo.getValue() != null) {
            pageSize = pageSizeCombo.getValue();
            currentPage = 0;
            refresh();
        }
    }

    @FXML
    private void handleAdd() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/PatientFormDialog.fxml", "Add Patient");
        PatientFormController controller = handle.controller();
        controller.setOnSaved(p -> refresh());
        handle.showAndWait();
    }

    @FXML
    private void handleExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Patients to Excel");
        chooser.setInitialFileName("hms-patients-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
        Stage stage = (Stage) patientTable.getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try {
            String keyword = searchField != null ? searchField.getText() : "";
            String gender = genderFilter != null && genderFilter.getValue() != null ? genderFilter.getValue() : "All";
            String blood = bloodGroupFilter != null && bloodGroupFilter.getValue() != null ? bloodGroupFilter.getValue() : "All";
            String sortBy = sortFilter != null && sortFilter.getValue() != null ? sortFilter.getValue() : "Name (A-Z)";

            Page<Patient> all = patientService.search(keyword, gender, blood, sortBy, 0, Integer.MAX_VALUE);
            excelExportService.exportPatients(all.items(), file);
            DialogUtil.showInfo("Export Complete", all.items().size() + " patient(s) exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            DialogUtil.showError("Export Failed", "Could not write the Excel file: " + e.getMessage());
        }
    }

    private void handleEdit(Patient patient) {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/PatientFormDialog.fxml", "Edit Patient");
        PatientFormController controller = handle.controller();
        controller.setPatientToEdit(patient);
        controller.setOnSaved(p -> refresh());
        handle.showAndWait();
    }

    private void handleRoom(Patient patient) {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/RoomAssignDialog.fxml", "Room Assignment");
        RoomAssignController controller = handle.controller();
        controller.setPatient(patient);
        controller.setOnChanged(this::refresh);
        handle.showAndWait();
    }

    private void handleDelete(Patient patient) {
        boolean confirmed = DialogUtil.confirm("Delete Patient",
                "Are you sure you want to delete " + patient.getFullName() + "? This cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            patientService.deletePatient(patient.getId());
            refresh();
        } catch (Exception e) {
            DialogUtil.showError("Delete Failed",
                    "Could not delete this patient. They may have existing appointments or medical records.");
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button roomBtn = new Button("Room");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, roomBtn, editBtn, deleteBtn);

            {
                roomBtn.getStyleClass().add("btn-secondary");
                editBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-danger");
                roomBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                roomBtn.setOnAction(e -> handleRoom(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
}