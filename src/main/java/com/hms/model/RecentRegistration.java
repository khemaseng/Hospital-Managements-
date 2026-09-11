package com.hms.model;

/**
 * A unified row for the dashboard's "Recent Registrations" panel, which
 * shows the newest Patients and Doctors interleaved in one table. Built
 * from Patient/Doctor rather than queried directly, since patients and
 * doctors are unrelated tables with no natural shared view.
 */
public record RecentRegistration(String type, String name, String code, String addedOn) {

    public static RecentRegistration fromPatient(Patient p) {
        String date = p.getRegisteredOn() != null ? p.getRegisteredOn().toString() : "";
        return new RecentRegistration("Patient", p.getFullName(), p.getPatientCode(), date);
    }

    public static RecentRegistration fromDoctor(Doctor d) {
        // Doctor has no client-visible "added on" date field, so the id
        // ordering already applied in DoctorRepository.findRecent() is
        // what determines its position in the merged/sorted feed.
        return new RecentRegistration("Doctor", d.getFullName(), d.getDoctorCode(), "");
    }
}
