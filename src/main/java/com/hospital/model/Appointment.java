package com.hospital.model;

import java.time.LocalDate;

/**
 * POJO (Plain Old Java Object) for an appointment.
 *
 * <p>An appointment links one patient to one doctor on one date in one
 * time slot. The {@code patientName} and {@code doctorName} fields are
 * filled in by JOIN queries for display only - they are not columns of
 * the appointment table (the table stores only the two foreign keys).</p>
 */
public class Appointment {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDate appointmentDate;
    private String timeSlot;   // "HH:MM", must be one of the fixed slots
    private String reason;
    private String status;     // BOOKED or COMPLETED

    // Display-only fields, filled by JOIN queries in the repository.
    private String patientName;
    private String doctorName;

    public Appointment() {
    }

    public Appointment(Long id, Long patientId, Long doctorId,
                       LocalDate appointmentDate, String timeSlot,
                       String reason, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.reason = reason;
        this.status = status;
    }

    public Appointment(Long patientId, Long doctorId, LocalDate appointmentDate,
                       String timeSlot, String reason) {
        this(null, patientId, doctorId, appointmentDate, timeSlot, reason, "BOOKED");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    @Override
    public String toString() {
        return String.format("Appointment[id=%d, patient=%s, doctor=%s, date=%s, slot=%s, status=%s, reason=%s]",
                id, patientName != null ? patientName : ("#" + patientId),
                doctorName != null ? doctorName : ("#" + doctorId),
                appointmentDate, timeSlot, status, reason);
    }
}
