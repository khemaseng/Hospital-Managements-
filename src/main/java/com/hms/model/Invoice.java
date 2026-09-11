package com.hms.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Invoice {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty invoiceCode = new SimpleStringProperty(this, "invoiceCode");
    private final IntegerProperty patientId = new SimpleIntegerProperty(this, "patientId");
    private final StringProperty patientName = new SimpleStringProperty(this, "patientName");
    private final IntegerProperty appointmentId = new SimpleIntegerProperty(this, "appointmentId");
    private final DoubleProperty consultationFee = new SimpleDoubleProperty(this, "consultationFee");
    private final DoubleProperty medicineFee = new SimpleDoubleProperty(this, "medicineFee");
    private final DoubleProperty labFee = new SimpleDoubleProperty(this, "labFee");
    private final DoubleProperty discount = new SimpleDoubleProperty(this, "discount");
    private final DoubleProperty tax = new SimpleDoubleProperty(this, "tax");
    private final DoubleProperty total = new SimpleDoubleProperty(this, "total");
    private final ObjectProperty<LocalDate> issuedOn = new SimpleObjectProperty<>(this, "issuedOn");
    private final ObjectProperty<PaymentStatus> paymentStatus = new SimpleObjectProperty<>(this, "paymentStatus", PaymentStatus.UNPAID);

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getInvoiceCode() { return invoiceCode.get(); }
    public void setInvoiceCode(String value) { invoiceCode.set(value); }
    public StringProperty invoiceCodeProperty() { return invoiceCode; }

    public int getPatientId() { return patientId.get(); }
    public void setPatientId(int value) { patientId.set(value); }
    public IntegerProperty patientIdProperty() { return patientId; }

    public String getPatientName() { return patientName.get(); }
    public void setPatientName(String value) { patientName.set(value); }
    public StringProperty patientNameProperty() { return patientName; }

    public int getAppointmentId() { return appointmentId.get(); }
    public void setAppointmentId(int value) { appointmentId.set(value); }
    public IntegerProperty appointmentIdProperty() { return appointmentId; }

    public double getConsultationFee() { return consultationFee.get(); }
    public void setConsultationFee(double value) { consultationFee.set(value); }
    public DoubleProperty consultationFeeProperty() { return consultationFee; }

    public double getMedicineFee() { return medicineFee.get(); }
    public void setMedicineFee(double value) { medicineFee.set(value); }
    public DoubleProperty medicineFeeProperty() { return medicineFee; }

    public double getLabFee() { return labFee.get(); }
    public void setLabFee(double value) { labFee.set(value); }
    public DoubleProperty labFeeProperty() { return labFee; }

    public double getDiscount() { return discount.get(); }
    public void setDiscount(double value) { discount.set(value); }
    public DoubleProperty discountProperty() { return discount; }

    public double getTax() { return tax.get(); }
    public void setTax(double value) { tax.set(value); }
    public DoubleProperty taxProperty() { return tax; }

    public double getTotal() { return total.get(); }
    public void setTotal(double value) { total.set(value); }
    public DoubleProperty totalProperty() { return total; }

    public LocalDate getIssuedOn() { return issuedOn.get(); }
    public void setIssuedOn(LocalDate value) { issuedOn.set(value); }
    public ObjectProperty<LocalDate> issuedOnProperty() { return issuedOn; }

    public PaymentStatus getPaymentStatus() { return paymentStatus.get(); }
    public void setPaymentStatus(PaymentStatus value) { paymentStatus.set(value); }
    public ObjectProperty<PaymentStatus> paymentStatusProperty() { return paymentStatus; }

    /** Consultation + medicine + lab, minus discount, plus tax. Called by the form before saving. */
    public void recomputeTotal() {
        double subtotal = getConsultationFee() + getMedicineFee() + getLabFee() - getDiscount();
        setTotal(Math.max(0, subtotal + getTax()));
    }
}
