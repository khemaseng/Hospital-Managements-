package com.hms.service;

import com.hms.model.Doctor;
import com.hms.repository.DoctorRepository;
import com.hms.util.Page;
import com.hms.util.ValidationException;
import com.hms.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class DoctorService {

    private final DoctorRepository doctorRepository = new DoctorRepository();

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Doctor> getRecentDoctors(int limit) {
        return doctorRepository.findRecent(limit);
    }

    public Page<Doctor> search(String keyword, String departmentFilter, int pageIndex, int pageSize) {
        List<Doctor> items = doctorRepository.search(keyword, departmentFilter, pageIndex, pageSize);
        int total = doctorRepository.countSearch(keyword, departmentFilter);
        return new Page<>(items, pageIndex, pageSize, total);
    }

    public Optional<Doctor> findById(int id) {
        return doctorRepository.findById(id);
    }

    public Doctor addDoctor(Doctor doctor) throws ValidationException {
        validate(doctor);
        doctor.setDoctorCode(doctorRepository.nextDoctorCode());
        return doctorRepository.save(doctor);
    }

    public void updateDoctor(Doctor doctor) throws ValidationException {
        validate(doctor);
        doctorRepository.update(doctor);
    }

    public void deleteDoctor(int id) {
        doctorRepository.findById(id).ifPresent(d ->
                new AuditService().log("DOCTOR_DELETED", "Deleted doctor " + d.getDoctorCode() + " (" + d.getFullName() + ")"));
        doctorRepository.deleteById(id);
    }

    public int countDoctors() {
        return doctorRepository.count();
    }

    private void validate(Doctor d) throws ValidationException {
        ValidationUtil.requireNonEmpty(d.getFullName(), "Full name");
        ValidationUtil.requireNonEmpty(d.getDepartment(), "Department");
        ValidationUtil.requireNonEmpty(d.getSpecialization(), "Specialization");
        ValidationUtil.validatePhone(d.getPhone());
        ValidationUtil.validateEmail(d.getEmail());
        if (d.getConsultationFee() < 0) {
            throw new ValidationException("Consultation fee cannot be negative.");
        }
    }
}
