package com.hms.service;

import com.hms.model.Invoice;
import com.hms.model.PaymentStatus;
import com.hms.repository.InvoiceRepository;
import com.hms.util.ValidationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BillingService {

    private final InvoiceRepository invoiceRepository = new InvoiceRepository();
    private final AuditService auditService = new AuditService();

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> search(String keyword) {
        return invoiceRepository.search(keyword);
    }

    public Optional<Invoice> findById(int id) {
        return invoiceRepository.findById(id);
    }

    public Invoice createInvoice(Invoice invoice) throws ValidationException {
        validate(invoice);
        invoice.recomputeTotal();
        invoice.setInvoiceCode(invoiceRepository.nextInvoiceCode());
        invoice.setIssuedOn(LocalDate.now());
        if (invoice.getPaymentStatus() == null) {
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        Invoice saved = invoiceRepository.save(invoice);
        auditService.log("INVOICE_CREATED", "Created invoice " + saved.getInvoiceCode()
                + " for $" + String.format("%.2f", saved.getTotal()));
        return saved;
    }

    public void updatePaymentStatus(int invoiceId, PaymentStatus status) {
        invoiceRepository.updatePaymentStatus(invoiceId, status);
        auditService.log("INVOICE_PAYMENT_STATUS_CHANGED", "Invoice #" + invoiceId + " marked " + status);
    }

    public void deleteInvoice(int id) {
        invoiceRepository.deleteById(id);
    }

    private void validate(Invoice inv) throws ValidationException {
        if (inv.getPatientId() <= 0) {
            throw new ValidationException("Please select a patient.");
        }
        if (inv.getConsultationFee() < 0 || inv.getMedicineFee() < 0 || inv.getLabFee() < 0
                || inv.getDiscount() < 0 || inv.getTax() < 0) {
            throw new ValidationException("Amounts cannot be negative.");
        }
        double subtotal = inv.getConsultationFee() + inv.getMedicineFee() + inv.getLabFee();
        if (inv.getDiscount() > subtotal) {
            throw new ValidationException("Discount cannot exceed the subtotal.");
        }
    }
}
