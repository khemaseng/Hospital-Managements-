package com.hms.service;

import com.hms.model.MedicalRecord;
import com.hms.repository.MedicalRecordRepository;
import com.hms.util.ValidationException;
import com.hms.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository = new MedicalRecordRepository();

    public List<MedicalRecord> getAllRecords() {
        return recordRepository.findAll();
    }

    public List<MedicalRecord> getRecordsForPatient(int patientId) {
        return recordRepository.findByPatient(patientId);
    }

    public List<MedicalRecord> search(String keyword) {
        return recordRepository.search(keyword);
    }

    public Optional<MedicalRecord> findById(int id) {
        return recordRepository.findById(id);
    }

    public MedicalRecord addRecord(MedicalRecord record) throws ValidationException {
        validate(record);
        record.setRecordDate(LocalDate.now());
        return recordRepository.save(record);
    }

    public void updateRecord(MedicalRecord record) throws ValidationException {
        validate(record);
        recordRepository.update(record);
    }

    public void deleteRecord(int id) {
        recordRepository.deleteById(id);
    }

    private void validate(MedicalRecord r) throws ValidationException {
        if (r.getPatientId() <= 0) {
            throw new ValidationException("Please select a patient.");
        }
        if (r.getDoctorId() <= 0) {
            throw new ValidationException("Please select a doctor.");
        }
        ValidationUtil.requireNonEmpty(r.getDiagnosis(), "Diagnosis");
    }
}
