package com.hospital.repository;

import com.hospital.model.Appointment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring-managed bean (@Repository) for appointments.
 *
 * <p>The list queries JOIN the patient and doctor tables so the display
 * can show names instead of bare ids. {@link #existsByDoctorSlot} is the
 * duplicate-booking check used by the service layer before any insert or
 * update; the UNIQUE constraint in schema.sql is the second line of
 * defence at the database level.</p>
 */
@Repository
public class AppointmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Appointment> ROW_MAPPER = (rs, rowNum) -> {
        Appointment a = new Appointment(
                rs.getLong("id"),
                rs.getLong("patient_id"),
                rs.getLong("doctor_id"),
                rs.getDate("appointment_date").toLocalDate(),
                rs.getString("time_slot"),
                rs.getString("reason"),
                rs.getString("status"));
        // Present in the JOIN queries; absent in the plain by-id query.
        try { a.setPatientName(rs.getString("patient_name")); } catch (Exception ignored) { }
        try { a.setDoctorName(rs.getString("doctor_name")); } catch (Exception ignored) { }
        return a;
    };

    private static final String SELECT_JOIN =
            "SELECT a.*, p.name AS patient_name, d.name AS doctor_name "
                    + "FROM appointment a "
                    + "JOIN patient p ON a.patient_id = p.id "
                    + "JOIN doctor d ON a.doctor_id = d.id ";

    public Appointment save(Appointment appointment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO appointment (patient_id, doctor_id, appointment_date, "
                            + "time_slot, reason, status) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, appointment.getPatientId());
            ps.setLong(2, appointment.getDoctorId());
            ps.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            ps.setString(4, appointment.getTimeSlot());
            ps.setString(5, appointment.getReason());
            ps.setString(6, appointment.getStatus());
            return ps;
        }, keyHolder);
        appointment.setId(keyHolder.getKey().longValue());
        return appointment;
    }

    public Optional<Appointment> findById(Long id) {
        List<Appointment> list = jdbcTemplate.query(
                SELECT_JOIN + "WHERE a.id = ?", ROW_MAPPER, id);
        return list.stream().findFirst();
    }

    public List<Appointment> findAll() {
        return jdbcTemplate.query(
                SELECT_JOIN + "ORDER BY a.appointment_date, a.time_slot", ROW_MAPPER);
    }

    public List<Appointment> findByPatient(Long patientId) {
        return jdbcTemplate.query(
                SELECT_JOIN + "WHERE a.patient_id = ? ORDER BY a.appointment_date, a.time_slot",
                ROW_MAPPER, patientId);
    }

    public List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date) {
        return jdbcTemplate.query(
                SELECT_JOIN + "WHERE a.doctor_id = ? AND a.appointment_date = ? "
                        + "ORDER BY a.time_slot",
                ROW_MAPPER, doctorId, Date.valueOf(date));
    }

    /** True when this doctor already has an appointment in this slot. */
    public boolean existsByDoctorSlot(Long doctorId, LocalDate date, String timeSlot) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment "
                        + "WHERE doctor_id = ? AND appointment_date = ? AND time_slot = ?",
                Integer.class, doctorId, Date.valueOf(date), timeSlot);
        return n != null && n > 0;
    }

    /** Same check, ignoring one appointment id (used when modifying it). */
    public boolean existsByDoctorSlotExcept(Long doctorId, LocalDate date,
                                            String timeSlot, Long excludeId) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment "
                        + "WHERE doctor_id = ? AND appointment_date = ? AND time_slot = ? "
                        + "AND id <> ?",
                Integer.class, doctorId, Date.valueOf(date), timeSlot, excludeId);
        return n != null && n > 0;
    }

    public int update(Appointment appointment) {
        return jdbcTemplate.update(
                "UPDATE appointment SET patient_id = ?, doctor_id = ?, "
                        + "appointment_date = ?, time_slot = ?, reason = ?, status = ? "
                        + "WHERE id = ?",
                appointment.getPatientId(), appointment.getDoctorId(),
                Date.valueOf(appointment.getAppointmentDate()), appointment.getTimeSlot(),
                appointment.getReason(), appointment.getStatus(), appointment.getId());
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM appointment WHERE id = ?", id);
    }

    public int count() {
        Integer n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointment", Integer.class);
        return n == null ? 0 : n;
    }
}
