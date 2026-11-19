package com.hospital.repository;

import com.hospital.model.Patient;
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
 * Spring-managed bean (@Repository) for patient records.
 *
 * <p>All database work goes through {@link JdbcTemplate}, which is Spring
 * JDBC: it opens the Connection, creates the PreparedStatement, binds the
 * parameters, executes, maps the ResultSet and closes everything. The SQL
 * uses ? placeholders, so user input is bound as data and can never break
 * out into SQL (no SQL injection). The RowMapper below turns each
 * ResultSet row into a Patient POJO.</p>
 */
@Repository
public class PatientRepository {

    private final JdbcTemplate jdbcTemplate;

    // Constructor injection: Spring supplies the JdbcTemplate bean.
    public PatientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Patient> ROW_MAPPER = (rs, rowNum) ->
            new Patient(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("phone"),
                    rs.getString("address")
            );

    public Patient save(Patient patient) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO patient (name, age, gender, phone, address) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getGender());
            ps.setString(4, patient.getPhone());
            ps.setString(5, patient.getAddress());
            return ps;
        }, keyHolder);
        patient.setId(keyHolder.getKey().longValue());
        return patient;
    }

    public Optional<Patient> findById(Long id) {
        List<Patient> list = jdbcTemplate.query(
                "SELECT * FROM patient WHERE id = ?", ROW_MAPPER, id);
        return list.stream().findFirst();
    }

    public List<Patient> findAll() {
        return jdbcTemplate.query("SELECT * FROM patient ORDER BY id", ROW_MAPPER);
    }

    public List<Patient> findByName(String namePart) {
        return jdbcTemplate.query(
                "SELECT * FROM patient WHERE LOWER(name) LIKE LOWER(?) ORDER BY id",
                ROW_MAPPER, "%" + namePart + "%");
    }

    public int update(Patient patient) {
        return jdbcTemplate.update(
                "UPDATE patient SET name = ?, age = ?, gender = ?, phone = ?, address = ? "
                        + "WHERE id = ?",
                patient.getName(), patient.getAge(), patient.getGender(),
                patient.getPhone(), patient.getAddress(), patient.getId());
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM patient WHERE id = ?", id);
    }

    public int count() {
        Integer n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM patient", Integer.class);
        return n == null ? 0 : n;
    }
}
