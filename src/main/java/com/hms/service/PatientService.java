
package com.hms.service;

import com.hms.model.Patient;
import com.hms.repository.PatientRepository;
import com.hms.util.Page;
import com.hms.util.ValidationException;
import com.hms.util.ValidationUtil;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

public class PatientService {

    private final PatientRepository patientRepository = new PatientRepository();

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public List<Patient> getRecentPatients(int limit) {
        return patientRepository.findRecent(limit);
    }

    /** Paginated, filtered & sorted search */
    public Page<Patient> search(String keyword, String genderFilter, String bloodGroupFilter, String sortBy, int pageIndex, int pageSize) {
        List<Patient> items = patientRepository.search(keyword, genderFilter, bloodGroupFilter, sortBy, pageIndex, pageSize);
        int total = patientRepository.countSearch(keyword, genderFilter, bloodGroupFilter);
        return new Page<>(items, pageIndex, pageSize, total);
    }

    // Overload សម្រាប់កូដណាដែលហៅរក method ចាស់
    public Page<Patient> search(String keyword, String genderFilter, String bloodGroupFilter, int pageIndex, int pageSize) {
        return search(keyword, genderFilter, bloodGroupFilter, "Name (A-Z)", pageIndex, pageSize);
    }

    public Optional<Patient> findById(int id) {
        return patientRepository.findById(id);
    }

    public Patient addPatient(Patient patient) throws ValidationException {
        validate(patient);
        patient.setPatientCode(patientRepository.nextPatientCode());
        patient.setRegisteredOn(LocalDate.now());
        return patientRepository.save(patient);
    }

    public void updatePatient(Patient patient) throws ValidationException {
        validate(patient);
        patientRepository.update(patient);
    }

    public void deletePatient(int id) {
        patientRepository.findById(id).ifPresent(p ->
                new AuditService().log("PATIENT_DELETED", "Deleted patient " + p.getPatientCode() + " (" + p.getFullName() + ")"));
        patientRepository.deleteById(id);
    }

    public int countPatients() {
        return patientRepository.count();
    }

    private void validate(Patient p) throws ValidationException {
        ValidationUtil.requireNonEmpty(p.getFullName(), "Full name");
        ValidationUtil.requireNonEmpty(p.getGender(), "Gender");
        ValidationUtil.validatePhone(p.getPhone());
        ValidationUtil.validateAge(p.getAge());

        if (p.getEmail() != null && !p.getEmail().isBlank()) {
            ValidationUtil.validateEmail(p.getEmail());
        }
        if (p.getDateOfBirth() != null) {
            ValidationUtil.validateNotFutureDate(p.getDateOfBirth(), "Date of birth");
            int computedAge = Period.between(p.getDateOfBirth(), LocalDate.now()).getYears();
            if (Math.abs(computedAge - p.getAge()) > 1) {
                throw new ValidationException("Age (" + p.getAge() + ") does not match date of birth.");
            }
        }
    }
}