package com.hospital.service;

import com.hospital.model.Patient;
import com.hospital.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Spring-managed bean (@Service) for patient business rules.
 *
 * <p>The service layer sits between the menu (UI) and the repository
 * (database). It validates input and enforces rules; it never writes SQL
 * itself. Spring creates one instance of this class and injects the
 * repository into it (dependency injection), so the menu only ever talks
 * to this service.</p>
 */
@Service
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public Patient register(String name, int age, String gender,
                            String phone, String address) {
        validate(name, age, gender, phone);
        return repository.save(new Patient(name, age, gender, phone, address));
    }

    public Optional<Patient> findById(Long id) {
        return repository.findById(id);
    }

    public List<Patient> listAll() {
        return repository.findAll();
    }

    public List<Patient> searchByName(String namePart) {
        return repository.findByName(namePart);
    }

    public boolean update(Patient patient) {
        validate(patient.getName(), patient.getAge(),
                patient.getGender(), patient.getPhone());
        return repository.update(patient) == 1;
    }

    /** Deletes the patient; their appointments go too (ON DELETE CASCADE). */
    public boolean delete(Long id) {
        return repository.deleteById(id) == 1;
    }

    public int count() {
        return repository.count();
    }

    private void validate(String name, int age, String gender, String phone) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Patient name must not be empty.");
        }
        if (age <= 0 || age >= 130) {
            throw new IllegalArgumentException("Age must be between 1 and 129.");
        }
        if (gender == null
                || !(gender.equalsIgnoreCase("Male")
                || gender.equalsIgnoreCase("Female")
                || gender.equalsIgnoreCase("Other"))) {
            throw new IllegalArgumentException("Gender must be Male, Female or Other.");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits.");
        }
    }
}
