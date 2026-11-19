package com.hospital;

import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for patient registration and validation. */
@SpringBootTest
@Transactional
class PatientServiceTest {

    @Autowired
    private PatientService service;

    @Test
    void registerAndFind() {
        Patient saved = service.register("Test User", 30, "Female", "9876543210", "Test Street");
        assertNotNull(saved.getId());
        assertTrue(service.findById(saved.getId()).isPresent());
        assertEquals("Test User", service.findById(saved.getId()).get().getName());
    }

    @Test
    void badPhoneIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register("Test User", 30, "Female", "123", "Test Street"));
    }

    @Test
    void badAgeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register("Test User", 0, "Female", "9876543210", "Test Street"));
    }
}
