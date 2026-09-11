package com.hms.repository;

import com.hms.database.DatabaseConnection;
import com.hms.model.Invoice;
import com.hms.model.PaymentStatus;
import com.hms.util.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceRepository {

    private static final String SELECT_JOINED =
            "SELECT i.*, p.full_name AS patient_name FROM invoices i JOIN patients p ON p.id = i.patient_id ";

    public List<Invoice> findAll() {
        String sql = SELECT_JOINED + "ORDER BY i.issued_on DESC, i.id DESC";
        List<Invoice> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load invoices.", e);
        }
        return result;
    }

    public List<Invoice> search(String keyword) {
        StringBuilder sql = new StringBuilder(SELECT_JOINED + "WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.full_name LIKE ? OR i.invoice_code LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY i.issued_on DESC, i.id DESC");

        List<Invoice> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to search invoices.", e);
        }
        return result;
    }

    public Optional<Invoice> findById(int id) {
        String sql = SELECT_JOINED + "WHERE i.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up invoice.", e);
        }
    }

    public String nextInvoiceCode() {
        String sql = "SELECT COUNT(*) AS cnt FROM invoices";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int count = rs.next() ? rs.getInt("cnt") : 0;
            return String.format("INV-%05d", count + 1);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to generate invoice code.", e);
        }
    }

    public Invoice save(Invoice inv) {
        String sql = "INSERT INTO invoices (invoice_code, patient_id, appointment_id, consultation_fee, " +
                     "medicine_fee, lab_fee, discount, tax, total, payment_status, issued_on) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, inv.getInvoiceCode());
            ps.setInt(2, inv.getPatientId());
            if (inv.getAppointmentId() > 0) {
                ps.setInt(3, inv.getAppointmentId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setDouble(4, inv.getConsultationFee());
            ps.setDouble(5, inv.getMedicineFee());
            ps.setDouble(6, inv.getLabFee());
            ps.setDouble(7, inv.getDiscount());
            ps.setDouble(8, inv.getTax());
            ps.setDouble(9, inv.getTotal());
            ps.setString(10, (inv.getPaymentStatus() != null ? inv.getPaymentStatus() : PaymentStatus.UNPAID).name());
            ps.setString(11, (inv.getIssuedOn() != null ? inv.getIssuedOn() : LocalDate.now()).toString());
            ps.executeUpdate();
            inv.setId(DatabaseConnection.lastInsertRowId(DatabaseConnection.getConnection()));
            return inv;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save invoice.", e);
        }
    }

    public void updatePaymentStatus(int invoiceId, PaymentStatus status) {
        String sql = "UPDATE invoices SET payment_status = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, invoiceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update payment status.", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM invoices WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete invoice.", e);
        }
    }

    private Invoice map(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setInvoiceCode(rs.getString("invoice_code"));
        inv.setPatientId(rs.getInt("patient_id"));
        inv.setPatientName(rs.getString("patient_name"));
        inv.setAppointmentId(rs.getInt("appointment_id"));
        inv.setConsultationFee(rs.getDouble("consultation_fee"));
        inv.setMedicineFee(rs.getDouble("medicine_fee"));
        inv.setLabFee(rs.getDouble("lab_fee"));
        inv.setDiscount(rs.getDouble("discount"));
        inv.setTax(rs.getDouble("tax"));
        inv.setTotal(rs.getDouble("total"));
        String status = rs.getString("payment_status");
        inv.setPaymentStatus(status != null ? PaymentStatus.valueOf(status) : PaymentStatus.UNPAID);
        String date = rs.getString("issued_on");
        if (date != null) {
            inv.setIssuedOn(LocalDate.parse(date));
        }
        return inv;
    }
}
