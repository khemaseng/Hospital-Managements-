package com.hms.service;

import com.hms.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Exports the app's normalized domain entities to styled .xlsx workbooks
 * using Apache POI. One sheet per export, with a bold/filled header row
 * and auto-sized columns - not just a raw data dump.
 *
 * Kept as a single small service (rather than one exporter class per
 * entity) since every export follows the exact same shape: a header row
 * plus one row per record, with only the column list and row-writer
 * differing per entity.
 */
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public void exportPatients(List<Patient> patients, File destination) throws IOException {
        String[] headers = {"Patient ID", "Full Name", "Gender", "Age", "Date of Birth",
                "Phone", "Email", "Address", "Blood Group", "Emergency Contact", "Room", "Registered On"};
        writeSheet(destination, "Patients", headers, patients, (row, p) -> {
            set(row, 0, p.getPatientCode());
            set(row, 1, p.getFullName());
            set(row, 2, p.getGender());
            set(row, 3, p.getAge());
            set(row, 4, p.getDateOfBirth() != null ? p.getDateOfBirth().format(DATE_FORMAT) : "");
            set(row, 5, p.getPhone());
            set(row, 6, p.getEmail());
            set(row, 7, p.getAddress());
            set(row, 8, p.getBloodGroup());
            set(row, 9, p.getEmergencyContact());
            set(row, 10, p.isAdmitted() ? p.getRoomCode() : "-");
            set(row, 11, p.getRegisteredOn() != null ? p.getRegisteredOn().format(DATE_FORMAT) : "");
        });
    }

    public void exportDoctors(List<Doctor> doctors, File destination) throws IOException {
        String[] headers = {"Doctor ID", "Full Name", "Department", "Specialization",
                "Phone", "Email", "Working Schedule", "Consultation Fee"};
        writeSheet(destination, "Doctors", headers, doctors, (row, d) -> {
            set(row, 0, d.getDoctorCode());
            set(row, 1, d.getFullName());
            set(row, 2, d.getDepartment());
            set(row, 3, d.getSpecialization());
            set(row, 4, d.getPhone());
            set(row, 5, d.getEmail());
            set(row, 6, d.getWorkingSchedule());
            set(row, 7, d.getConsultationFee());
        });
    }

    public void exportAppointments(List<Appointment> appointments, File destination) throws IOException {
        String[] headers = {"Appointment ID", "Patient", "Doctor", "Department",
                "Date", "Time", "Status", "Reason"};
        writeSheet(destination, "Appointments", headers, appointments, (row, a) -> {
            set(row, 0, a.getAppointmentCode());
            set(row, 1, a.getPatientName());
            set(row, 2, a.getDoctorName());
            set(row, 3, a.getDepartment());
            set(row, 4, a.getAppointmentDate() != null ? a.getAppointmentDate().format(DATE_FORMAT) : "");
            set(row, 5, a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "");
            set(row, 6, a.getStatus() != null ? a.getStatus().name() : "");
            set(row, 7, a.getReason());
        });
    }

    public void exportInvoices(List<Invoice> invoices, File destination) throws IOException {
        String[] headers = {"Invoice ID", "Patient", "Consultation Fee", "Medicine Fee",
                "Lab Fee", "Discount", "Tax", "Total", "Payment Status", "Issued On"};
        writeSheet(destination, "Invoices", headers, invoices, (row, i) -> {
            set(row, 0, i.getInvoiceCode());
            set(row, 1, i.getPatientName());
            set(row, 2, i.getConsultationFee());
            set(row, 3, i.getMedicineFee());
            set(row, 4, i.getLabFee());
            set(row, 5, i.getDiscount());
            set(row, 6, i.getTax());
            set(row, 7, i.getTotal());
            set(row, 8, i.getPaymentStatus() != null ? i.getPaymentStatus().name() : "");
            set(row, 9, i.getIssuedOn() != null ? i.getIssuedOn().format(DATE_FORMAT) : "");
        });
    }

    public void exportMedicalRecords(List<MedicalRecord> records, File destination) throws IOException {
        String[] headers = {"Date", "Patient", "Doctor", "Diagnosis", "Symptoms",
                "Prescription", "Lab Result", "Doctor Notes"};
        writeSheet(destination, "Medical Records", headers, records, (row, r) -> {
            set(row, 0, r.getRecordDate() != null ? r.getRecordDate().format(DATE_FORMAT) : "");
            set(row, 1, r.getPatientName());
            set(row, 2, r.getDoctorName());
            set(row, 3, r.getDiagnosis());
            set(row, 4, r.getSymptoms());
            set(row, 5, r.getPrescription());
            set(row, 6, r.getLabResult());
            set(row, 7, r.getDoctorNotes());
        });
    }

    /**
     * Shared workhorse: creates one sheet with a styled header row, hands
     * each record to the caller-supplied row-writer, then auto-sizes every
     * column. `rowWriter` writes into a small mutable RowCells wrapper
     * (defined below) rather than a raw POI Row, so entity-specific export
     * methods above don't need to import POI classes at all.
     */
    private <T> void writeSheet(File destination, String sheetName, String[] headers,
                                 List<T> items, BiConsumer<RowCells, T> rowWriter) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (T item : items) {
                Row row = sheet.createRow(rowIndex++);
                rowWriter.accept(new RowCells(row), item);
            }

            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
                if (sheet.getColumnWidth(col) < 2500) {
                    sheet.setColumnWidth(col, 2500);
                }
            }
            sheet.createFreezePane(0, 1);

            try (FileOutputStream out = new FileOutputStream(destination)) {
                workbook.write(out);
            }
        }
    }

    private void set(RowCells row, int col, String value) {
        row.setString(col, value);
    }

    private void set(RowCells row, int col, int value) {
        row.setNumber(col, value);
    }

    private void set(RowCells row, int col, double value) {
        row.setNumber(col, value);
    }

    /** Thin wrapper so entity export methods above never need a POI import. */
    private static final class RowCells {
        private final Row row;

        RowCells(Row row) {
            this.row = row;
        }

        void setString(int col, String value) {
            row.createCell(col).setCellValue(value != null ? value : "");
        }

        void setNumber(int col, double value) {
            row.createCell(col).setCellValue(value);
        }
    }
}
