package com.hospital.ui;

import com.hospital.jdbc.RawJdbcDemo;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Console user interface (Spring bean).
 *
 * <p>The menu talks ONLY to the service layer - it never touches the
 * database or the repositories. That is the point of the layered design:
 * UI -> service (rules) -> repository (SQL) -> database.</p>
 *
 * <p>It is started explicitly from {@code HospitalApplication.main()}
 * after the Spring context is up. It deliberately does NOT implement
 * CommandLineRunner: a runner would execute whenever the context
 * starts, including inside tests, and the menu loop would then wait
 * for keyboard input that never comes.</p>
 */
@Component
public class ConsoleMenu {

    private final PatientService patients;
    private final DoctorService doctors;
    private final AppointmentService appointments;
    private final RawJdbcDemo rawJdbcDemo;
    private final Scanner in = new Scanner(System.in);

    public ConsoleMenu(PatientService patients, DoctorService doctors,
                       AppointmentService appointments, RawJdbcDemo rawJdbcDemo) {
        this.patients = patients;
        this.doctors = doctors;
        this.appointments = appointments;
        this.rawJdbcDemo = rawJdbcDemo;
    }

    /** Starts the menu loop. Called once from main(). */
    public void start() {
        rawJdbcDemo.showCounts();
        System.out.println("============================================================");
        System.out.println("  HOSPITAL APPOINTMENT AND PATIENT RECORD MANAGEMENT");
        System.out.println("  Spring Boot + JdbcTemplate + H2 (embedded, via JDBC)");
        System.out.println("============================================================");
        System.out.println("Loaded: " + patients.count() + " patients, "
                + doctors.count() + " doctors, "
                + appointments.count() + " appointments.\n");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Enter choice: ");
            try {
                switch (choice) {
                    case "1" -> registerPatient();
                    case "2" -> listPatients();
                    case "3" -> searchPatient();
                    case "4" -> updatePatient();
                    case "5" -> deletePatient();
                    case "6" -> addDoctor();
                    case "7" -> listDoctors();
                    case "8" -> searchDoctors();
                    case "9" -> bookAppointment();
                    case "10" -> listAppointments();
                    case "11" -> appointmentsByPatient();
                    case "12" -> doctorSchedule();
                    case "13" -> modifyAppointment();
                    case "14" -> cancelAppointment();
                    case "15" -> completeAppointment();
                    case "0" -> {
                        running = false;
                        System.out.println("Thank you. Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Please enter a number from the menu.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("---- PATIENTS ----");
        System.out.println(" 1. Register patient");
        System.out.println(" 2. List all patients");
        System.out.println(" 3. Search patient by name");
        System.out.println(" 4. Update patient");
        System.out.println(" 5. Delete patient");
        System.out.println("---- DOCTORS ----");
        System.out.println(" 6. Add doctor");
        System.out.println(" 7. List all doctors");
        System.out.println(" 8. Search doctors by specialization");
        System.out.println("---- APPOINTMENTS ----");
        System.out.println(" 9. Book appointment");
        System.out.println("10. View all appointments");
        System.out.println("11. View appointments by patient");
        System.out.println("12. View a doctor's day schedule (with free slots)");
        System.out.println("13. Modify appointment");
        System.out.println("14. Cancel appointment");
        System.out.println("15. Mark appointment completed");
        System.out.println(" 0. Exit");
    }

    // ---------------- patients ----------------

    private void registerPatient() {
        System.out.println("-- Register patient --");
        String name = readLine("Name: ");
        int age = readInt("Age: ");
        String gender = readLine("Gender (Male/Female/Other): ");
        String phone = readLine("Phone (10 digits): ");
        String address = readLine("Address: ");
        Patient saved = patients.register(name, age, gender, phone, address);
        System.out.println("Registered: " + saved);
    }

    private void listPatients() {
        List<Patient> all = patients.listAll();
        if (all.isEmpty()) {
            System.out.println("No patients registered yet.");
            return;
        }
        System.out.printf("%-5s %-20s %-5s %-8s %-12s %s%n",
                "ID", "Name", "Age", "Gender", "Phone", "Address");
        for (Patient p : all) {
            System.out.printf("%-5d %-20s %-5d %-8s %-12s %s%n",
                    p.getId(), p.getName(), p.getAge(), p.getGender(), p.getPhone(), p.getAddress());
        }
    }

    private void searchPatient() {
        String name = readLine("Enter name (or part of it): ");
        List<Patient> found = patients.searchByName(name);
        if (found.isEmpty()) {
            System.out.println("No patient matches '" + name + "'.");
        } else {
            found.forEach(System.out::println);
        }
    }

    private void updatePatient() {
        Long id = readLong("Patient ID to update: ");
        Patient p = patients.findById(id).orElse(null);
        if (p == null) {
            System.out.println("No patient with id " + id + ".");
            return;
        }
        System.out.println("Current: " + p);
        System.out.println("Press Enter to keep the current value.");
        p.setName(keepOrReplace("Name [" + p.getName() + "]: ", p.getName()));
        String age = readLine("Age [" + p.getAge() + "]: ");
        if (!age.isBlank()) {
            p.setAge(Integer.parseInt(age));
        }
        p.setGender(keepOrReplace("Gender [" + p.getGender() + "]: ", p.getGender()));
        p.setPhone(keepOrReplace("Phone [" + p.getPhone() + "]: ", p.getPhone()));
        p.setAddress(keepOrReplace("Address [" + p.getAddress() + "]: ", p.getAddress()));
        System.out.println(patients.update(p) ? "Patient updated." : "Update failed.");
    }

    private void deletePatient() {
        Long id = readLong("Patient ID to delete: ");
        String confirm = readLine("This also deletes their appointments. Type YES to confirm: ");
        if (!confirm.equalsIgnoreCase("YES")) {
            System.out.println("Delete cancelled.");
            return;
        }
        System.out.println(patients.delete(id)
                ? "Patient deleted." : "No patient with id " + id + ".");
    }

    // ---------------- doctors ----------------

    private void addDoctor() {
        System.out.println("-- Add doctor --");
        String name = readLine("Name (e.g. Dr. Meena Shah): ");
        String spec = readLine("Specialization: ");
        String phone = readLine("Phone (10 digits): ");
        double fee = readDouble("Consultation fee (Rs.): ");
        Doctor saved = doctors.add(name, spec, phone, fee);
        System.out.println("Added: " + saved);
    }

    private void listDoctors() {
        List<Doctor> all = doctors.listAll();
        if (all.isEmpty()) {
            System.out.println("No doctors registered yet.");
            return;
        }
        System.out.printf("%-5s %-20s %-18s %-12s %s%n",
                "ID", "Name", "Specialization", "Phone", "Fee (Rs.)");
        for (Doctor d : all) {
            System.out.printf("%-5d %-20s %-18s %-12s %.2f%n",
                    d.getId(), d.getName(), d.getSpecialization(), d.getPhone(), d.getConsultFee());
        }
    }

    private void searchDoctors() {
        String spec = readLine("Specialization (e.g. Cardiology): ");
        List<Doctor> found = doctors.findBySpecialization(spec);
        if (found.isEmpty()) {
            System.out.println("No doctor found for '" + spec + "'.");
        } else {
            found.forEach(System.out::println);
        }
    }

    // ---------------- appointments ----------------

    private void bookAppointment() {
        System.out.println("-- Book appointment --");
        listDoctors();
        Long doctorId = readLong("Doctor ID: ");
        LocalDate date = readDate("Date (yyyy-MM-dd): ");
        System.out.println("Free slots for this doctor on " + date + ": "
                + appointments.freeSlots(doctorId, date));
        String slot = readLine("Time slot (HH:MM): ");
        listPatients();
        Long patientId = readLong("Patient ID: ");
        String reason = readLine("Reason for visit: ");
        Appointment saved = appointments.book(patientId, doctorId, date, slot, reason);
        System.out.println("Booked: " + saved);
    }

    private void listAppointments() {
        printAppointments(appointments.listAll());
    }

    private void appointmentsByPatient() {
        Long patientId = readLong("Patient ID: ");
        printAppointments(appointments.listByPatient(patientId));
    }

    private void doctorSchedule() {
        Long doctorId = readLong("Doctor ID: ");
        LocalDate date = readDate("Date (yyyy-MM-dd): ");
        System.out.println("Schedule for doctor " + doctorId + " on " + date + ":");
        printAppointments(appointments.doctorSchedule(doctorId, date));
        System.out.println("Free slots: " + appointments.freeSlots(doctorId, date));
    }

    private void modifyAppointment() {
        Long id = readLong("Appointment ID to modify: ");
        Appointment a = appointments.findById(id).orElse(null);
        if (a == null) {
            System.out.println("No appointment with id " + id + ".");
            return;
        }
        System.out.println("Current: " + a);
        Long doctorId = readLong("New doctor ID [" + a.getDoctorId() + "]: ", a.getDoctorId());
        LocalDate date = readDate("New date (yyyy-MM-dd) [" + a.getAppointmentDate() + "]: ",
                a.getAppointmentDate());
        System.out.println("Free slots for this doctor on " + date + ": "
                + appointments.freeSlots(doctorId, date));
        String slot = keepOrReplace("New time slot [" + a.getTimeSlot() + "]: ", a.getTimeSlot());
        String reason = keepOrReplace("New reason [" + a.getReason() + "]: ", a.getReason());
        Appointment updated = appointments.modify(id, a.getPatientId(), doctorId, date, slot, reason);
        System.out.println("Modified: " + updated);
    }

    private void cancelAppointment() {
        Long id = readLong("Appointment ID to cancel: ");
        System.out.println(appointments.cancel(id)
                ? "Appointment cancelled. The slot is free again."
                : "No appointment with id " + id + ".");
    }

    private void completeAppointment() {
        Long id = readLong("Appointment ID to mark completed: ");
        System.out.println(appointments.complete(id)
                ? "Appointment marked COMPLETED."
                : "No appointment with id " + id + ".");
    }

    private void printAppointments(List<Appointment> list) {
        if (list.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }
        System.out.printf("%-5s %-18s %-20s %-12s %-7s %-10s %s%n",
                "ID", "Patient", "Doctor", "Date", "Slot", "Status", "Reason");
        for (Appointment a : list) {
            System.out.printf("%-5d %-18s %-20s %-12s %-7s %-10s %s%n",
                    a.getId(), a.getPatientName(), a.getDoctorName(),
                    a.getAppointmentDate(), a.getTimeSlot(), a.getStatus(), a.getReason());
        }
    }

    // ---------------- input helpers ----------------

    private String readLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private String keepOrReplace(String prompt, String current) {
        String input = readLine(prompt);
        return input.isBlank() ? current : input;
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private Long readLong(String prompt) {
        while (true) {
            try {
                return Long.parseLong(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private Long readLong(String prompt, Long defaultValue) {
        String input = readLine(prompt);
        if (input.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            System.out.println("Not a number - keeping " + defaultValue + ".");
            return defaultValue;
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readLine(prompt));
            } catch (DateTimeParseException e) {
                System.out.println("Please enter a date as yyyy-MM-dd (e.g. 2026-11-16).");
            }
        }
    }

    private LocalDate readDate(String prompt, LocalDate defaultValue) {
        String input = readLine(prompt);
        if (input.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Bad date - keeping " + defaultValue + ".");
            return defaultValue;
        }
    }
}
