package com.hospital.service;

import com.hospital.model.Doctor;
import com.hospital.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Spring-managed bean (@Service) for doctor business rules.
 * Same layered pattern as {@link PatientService}.
 */
@Service
public class DoctorService {

    private final DoctorRepository repository;

    public DoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    public Doctor add(String name, String specialization, String phone, double consultFee) {
        validate(name, specialization, phone, consultFee);
        return repository.save(new Doctor(name, specialization, phone, consultFee));
    }

    public Optional<Doctor> findById(Long id) {
        return repository.findById(id);
    }

    public List<Doctor> listAll() {
        return repository.findAll();
    }

    public List<Doctor> findBySpecialization(String specialization) {
        return repository.findBySpecialization(specialization);
    }

    public boolean update(Doctor doctor) {
        validate(doctor.getName(), doctor.getSpecialization(),
                doctor.getPhone(), doctor.getConsultFee());
        return repository.update(doctor) == 1;
    }

    /** Deletes the doctor; their appointments go too (ON DELETE CASCADE). */
    public boolean delete(Long id) {
        return repository.deleteById(id) == 1;
    }

    public int count() {
        return repository.count();
    }

    private void validate(String name, String specialization, String phone, double consultFee) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Doctor name must not be empty.");
        }
        if (specialization == null || specialization.isBlank()) {
            throw new IllegalArgumentException("Specialization must not be empty.");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits.");
        }
        if (consultFee < 0) {
            throw new IllegalArgumentException("Consultation fee must not be negative.");
        }
    }
}
