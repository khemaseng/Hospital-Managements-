package com.hms.service;

import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;
import com.hms.repository.AppointmentRepository;
import com.hms.util.ValidationException;
import com.hms.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Owns the double-booking rule: a doctor cannot have two SCHEDULED
 * appointments at the same date+time. The check happens here (business
 * rule) and is backstopped by a UNIQUE constraint in the schema
 * (data-integrity rule) in case of concurrent writers.
 */
public class AppointmentService {

    private final AppointmentRepository appointmentRepository = new AppointmentRepository();

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsForDate(LocalDate date) {
        return appointmentRepository.findByDate(date);
    }

    public List<Appointment> search(String keyword, String statusFilter) {
        return appointmentRepository.search(keyword, statusFilter);
    }

    public Optional<Appointment> findById(int id) {
        return appointmentRepository.findById(id);
    }

    public Appointment bookAppointment(Appointment appointment) throws ValidationException {
        validate(appointment);
        if (appointmentRepository.hasConflict(appointment.getDoctorId(), appointment.getAppointmentDate(),
                appointment.getAppointmentTime(), null)) {
            throw new ValidationException("This doctor already has an appointment at that date and time. " +
                    "Please choose a different slot.");
        }
        appointment.setAppointmentCode(appointmentRepository.nextAppointmentCode());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    public void reschedule(int appointmentId, int doctorId, LocalDate newDate, LocalTime newTime)
            throws ValidationException {
        ValidationUtil.validateAppointmentDate(newDate);
        if (appointmentRepository.hasConflict(doctorId, newDate, newTime, appointmentId)) {
            throw new ValidationException("This doctor already has an appointment at that date and time.");
        }
        appointmentRepository.reschedule(appointmentId, newDate, newTime);
    }

    public void updateStatus(int appointmentId, AppointmentStatus status) {
        appointmentRepository.updateStatus(appointmentId, status);
    }

    public void cancelAppointment(int appointmentId) {
        appointmentRepository.updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    public void deleteAppointment(int id) {
        appointmentRepository.deleteById(id);
    }

    public int countTotal() {
        return appointmentRepository.countTotal();
    }

    public int countToday() {
        return appointmentRepository.countToday();
    }

    private void validate(Appointment a) throws ValidationException {
        if (a.getPatientId() <= 0) {
            throw new ValidationException("Please select a patient.");
        }
        if (a.getDoctorId() <= 0) {
            throw new ValidationException("Please select a doctor.");
        }
        ValidationUtil.requireNonEmpty(a.getDepartment(), "Department");
        ValidationUtil.validateAppointmentDate(a.getAppointmentDate());
        if (a.getAppointmentTime() == null) {
            throw new ValidationException("Please select an appointment time.");
        }
    }
}
