package com.hospital;

import com.hospital.model.Appointment;
import com.hospital.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the appointment booking rules. Each test runs in a
 * transaction that is rolled back afterwards, so tests never change
 * the seeded data. Dates are far in the future so they can never
 * clash with the sample appointments in data.sql.
 */
@SpringBootTest
@Transactional
class AppointmentServiceTest {

    @Autowired
    private AppointmentService service;

    private static final LocalDate DAY = LocalDate.of(2027, 3, 10);

    @Test
    void bookingASlotWorks() {
        Appointment a = service.book(1L, 1L, DAY, "09:00", "Test visit");
        assertNotNull(a.getId());
        assertEquals("BOOKED", a.getStatus());
        assertEquals(DAY, a.getAppointmentDate());
    }

    @Test
    void duplicateBookingIsRejected() {
        service.book(1L, 1L, DAY, "09:30", "First booking");
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.book(2L, 1L, DAY, "09:30", "Second booking"));
        assertTrue(e.getMessage().contains("already booked"));
    }

    @Test
    void sameSlotDifferentDoctorIsAllowed() {
        service.book(1L, 1L, DAY, "10:00", "Doctor 1");
        Appointment other = service.book(2L, 2L, DAY, "10:00", "Doctor 2");
        assertNotNull(other.getId());
    }

    @Test
    void cancelFreesTheSlot() {
        Appointment a = service.book(1L, 1L, DAY, "10:30", "To cancel");
        assertTrue(service.cancel(a.getId()));
        Appointment again = service.book(2L, 1L, DAY, "10:30", "After cancel");
        assertNotNull(again.getId());
    }

    @Test
    void modifyToTakenSlotIsRejected() {
        service.book(1L, 1L, DAY, "11:00", "Takes 11:00");
        Appointment movable = service.book(2L, 1L, DAY, "11:30", "Takes 11:30");
        assertThrows(IllegalStateException.class,
                () -> service.modify(movable.getId(), 2L, 1L, DAY, "11:00", "Move to 11:00"));
    }

    @Test
    void invalidSlotIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.book(1L, 1L, DAY, "13:00", "Lunch hour is not a slot"));
    }

    @Test
    void unknownPatientIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.book(999L, 1L, DAY, "09:00", "No such patient"));
    }
}
