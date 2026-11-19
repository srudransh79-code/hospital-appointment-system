package com.hospital.repository;

import com.hospital.model.Doctor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * Spring-managed bean (@Repository) for doctor records.
 * Same pattern as {@link PatientRepository}: JdbcTemplate + RowMapper.
 */
@Repository
public class DoctorRepository {

    private final JdbcTemplate jdbcTemplate;

    public DoctorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Doctor> ROW_MAPPER = (rs, rowNum) ->
            new Doctor(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("phone"),
                    rs.getDouble("consult_fee")
            );

    public Doctor save(Doctor doctor) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO doctor (name, specialization, phone, consult_fee) "
                            + "VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getPhone());
            ps.setDouble(4, doctor.getConsultFee());
            return ps;
        }, keyHolder);
        doctor.setId(keyHolder.getKey().longValue());
        return doctor;
    }

    public Optional<Doctor> findById(Long id) {
        List<Doctor> list = jdbcTemplate.query(
                "SELECT * FROM doctor WHERE id = ?", ROW_MAPPER, id);
        return list.stream().findFirst();
    }

    public List<Doctor> findAll() {
        return jdbcTemplate.query("SELECT * FROM doctor ORDER BY id", ROW_MAPPER);
    }

    public List<Doctor> findBySpecialization(String specialization) {
        return jdbcTemplate.query(
                "SELECT * FROM doctor WHERE LOWER(specialization) = LOWER(?) ORDER BY id",
                ROW_MAPPER, specialization);
    }

    public int update(Doctor doctor) {
        return jdbcTemplate.update(
                "UPDATE doctor SET name = ?, specialization = ?, phone = ?, consult_fee = ? "
                        + "WHERE id = ?",
                doctor.getName(), doctor.getSpecialization(),
                doctor.getPhone(), doctor.getConsultFee(), doctor.getId());
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM doctor WHERE id = ?", id);
    }

    public int count() {
        Integer n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM doctor", Integer.class);
        return n == null ? 0 : n;
    }
}
