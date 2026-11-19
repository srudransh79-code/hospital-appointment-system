package com.hospital.service;

import com.hospital.model.Appointment;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring-managed bean (@Service) for appointment booking rules.
 *
 * <p>This is where the case study's key rule lives: <b>no duplicate
 * booking for the same doctor and time slot</b>. Every booking (and every
 * modification) checks first through
 * {@link AppointmentRepository#existsByDoctorSlot}. If the slot is taken,
 * the booking is refused with a clear message and nothing is written. The
 * UNIQUE constraint on (doctor_id, appointment_date, time_slot) in
 * schema.sql enforces the same rule inside the database as a second line
 * of defence.</p>
 *
 * <p>Time slots are fixed: the hospital works in 30-minute slots from
 * 09:00 to 12:00 and 14:00 to 17:00. A booking outside these slots is
 * rejected, so two bookings can only ever collide on an exact slot.</p>
 */
@Service
public class AppointmentService {

    /** The hospital's fixed 30-minute slots. */
    public static final List<String> TIME_SLOTS = List.of(
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "14:00", "14:30", "15:00", "15:30", "16:00", "16:30");

    private final AppointmentRepository appointments;
    private final PatientRepository patients;
    private final DoctorRepository doctors;

    public AppointmentService(AppointmentRepository appointments,
                              PatientRepository patients,
                              DoctorRepository doctors) {
        this.appointments = appointments;
        this.patients = patients;
        this.doctors = doctors;
    }

    /**
     * Books an appointment. Returns the saved appointment, or throws
     * {@link IllegalStateException} if the slot is already taken and
     * {@link IllegalArgumentException} for bad input.
     */
    public Appointment book(Long patientId, Long doctorId, LocalDate date,
                            String timeSlot, String reason) {
        validateCommon(patientId, doctorId, date, timeSlot);
        if (appointments.existsByDoctorSlot(doctorId, date, timeSlot)) {
            throw new IllegalStateException(
                    "Slot already booked: doctor " + doctorId
                            + " is not free on " + date + " at " + timeSlot + ".");
        }
        Appointment saved = appointments.save(
                new Appointment(patientId, doctorId, date, timeSlot, reason));
        // Re-read through the JOIN query so the confirmation shows the
        // patient and doctor names, not just their ids.
        return appointments.findById(saved.getId()).orElse(saved);
    }

    /**
     * Modifies an existing appointment (new doctor, date or slot). The
     * duplicate check ignores the appointment being modified, so keeping
     * the same slot is allowed.
     */
    public Appointment modify(Long appointmentId, Long patientId, Long doctorId,
                              LocalDate date, String timeSlot, String reason) {
        Appointment existing = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No appointment with id " + appointmentId + "."));
        validateCommon(patientId, doctorId, date, timeSlot);
        if (appointments.existsByDoctorSlotExcept(doctorId, date, timeSlot, appointmentId)) {
            throw new IllegalStateException(
                    "Slot already booked: doctor " + doctorId
                            + " is not free on " + date + " at " + timeSlot + ".");
        }
        existing.setPatientId(patientId);
        existing.setDoctorId(doctorId);
        existing.setAppointmentDate(date);
        existing.setTimeSlot(timeSlot);
        existing.setReason(reason);
        appointments.update(existing);
        return appointments.findById(appointmentId).orElse(existing);
    }

    /** Cancels an appointment (deletes it, freeing the slot). */
    public boolean cancel(Long appointmentId) {
        return appointments.deleteById(appointmentId) == 1;
    }

    /** Marks an appointment completed. */
    public boolean complete(Long appointmentId) {
        Optional<Appointment> found = appointments.findById(appointmentId);
        if (found.isEmpty()) {
            return false;
        }
        Appointment a = found.get();
        a.setStatus("COMPLETED");
        return appointments.update(a) == 1;
    }

    public Optional<Appointment> findById(Long id) {
        return appointments.findById(id);
    }

    public List<Appointment> listAll() {
        return appointments.findAll();
    }

    public List<Appointment> listByPatient(Long patientId) {
        return appointments.findByPatient(patientId);
    }

    /** A doctor's day schedule: all appointments on one date, by slot. */
    public List<Appointment> doctorSchedule(Long doctorId, LocalDate date) {
        return appointments.findByDoctorAndDate(doctorId, date);
    }

    /** The slots of a doctor's day that are still free. */
    public List<String> freeSlots(Long doctorId, LocalDate date) {
        List<String> taken = appointments.findByDoctorAndDate(doctorId, date)
                .stream().map(Appointment::getTimeSlot).toList();
        return TIME_SLOTS.stream().filter(s -> !taken.contains(s)).toList();
    }

    public int count() {
        return appointments.count();
    }

    private void validateCommon(Long patientId, Long doctorId,
                                LocalDate date, String timeSlot) {
        if (patients.findById(patientId).isEmpty()) {
            throw new IllegalArgumentException("No patient with id " + patientId + ".");
        }
        if (doctors.findById(doctorId).isEmpty()) {
            throw new IllegalArgumentException("No doctor with id " + doctorId + ".");
        }
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date must be today or later.");
        }
        if (timeSlot == null || !TIME_SLOTS.contains(timeSlot)) {
            throw new IllegalArgumentException(
                    "Time slot must be one of " + TIME_SLOTS + ".");
        }
    }
}
