# Hospital Appointment and Patient Record Management System

OOPs Using Java — Case Study Project (Topic 5).

A Spring Boot console application that manages patient records, doctor
details and appointment bookings, with all data stored in a database
through JDBC.

**Students:** Rudransh Singh (UID: 25BCS10630), Aditya Pati Tripathi
(UID: 25BCS11374), Arjun Kumar Yadav (UID: 25BCS11064) — B.E. Computer
Science & Engineering, Section 802-B, Chandigarh University

## What it does

- **Patients** — register, list, search by name, update, delete.
- **Doctors** — add, list, search by specialization, update, delete.
- **Appointments** — book, view (all / by patient / by doctor and date),
  modify, cancel, mark completed.
- **Duplicate booking is prevented** — the same doctor cannot be booked
  twice in the same time slot on the same day. The service layer checks
  first and reports a clear message, and a UNIQUE constraint in the
  database enforces the same rule as a second line of defence.
- **Fixed time slots** — 30-minute slots, 09:00–12:00 and 14:00–17:00.
  The booking screen shows the doctor's free slots before you choose.

## How to run

Requirements: **JDK 17+** and **Maven** (no database server to install —
the database is embedded and starts with the application).

```bash
mvn spring-boot:run
```

Or build a runnable jar and run it:

```bash
mvn clean package
java -jar target/hospital-appointment-system-1.0.0.jar
```

Then use the numbered menu. Sample data (4 doctors, 5 patients,
3 appointments on 2026-11-16) is loaded on every start, so the demo
always begins from the same state.

Run the tests:

```bash
mvn test
```

## How the case-study requirements are met

| Requirement | Where it is done |
|---|---|
| Patient registration and record management | `PatientService` + `PatientRepository`, menu options 1–5 |
| Doctor details and appointment schedules | `DoctorService` + `DoctorRepository`, menu options 6–8; schedule view option 12 |
| Book, modify, cancel appointments | `AppointmentService`, menu options 9, 13, 14 |
| Prevent duplicate bookings (same doctor + slot) | `AppointmentService.book/modify` check + `UNIQUE(doctor_id, appointment_date, time_slot)` in `schema.sql` |
| JDBC statements and connection interfaces | `jdbc/RawJdbcDemo.java` uses `DriverManager`, `Connection`, `PreparedStatement`, `ResultSet` directly |
| Spring Boot annotations, Spring Beans, JdbcTemplate | `@SpringBootApplication`, `@Service`, `@Repository`, `@Component`, constructor injection; all repositories use `JdbcTemplate` |
| POJO vs Spring Bean comparison | See below, and report Chapter 3 |

## Project structure

```
src/main/java/com/hospital/
  HospitalApplication.java     entry point (@SpringBootApplication)
  model/                       POJOs: Patient, Doctor, Appointment
  repository/                  @Repository beans, JdbcTemplate + RowMapper
  service/                     @Service beans, business rules + validation
  jdbc/                        RawJdbcDemo (plain JDBC, for comparison)
  ui/                          ConsoleMenu (menu, started from main)
src/main/resources/
  application.properties       H2 embedded datasource
  schema.sql                   3 tables, foreign keys, UNIQUE slot guard
  data.sql                     sample doctors, patients, appointments
src/test/java/com/hospital/    JUnit tests (booking rules, validation)
```

## POJO vs Spring-managed Bean

| | POJO | Spring Bean |
|---|---|---|
| **Examples here** | `Patient`, `Doctor`, `Appointment` | `PatientService`, `AppointmentRepository`, `ConsoleMenu`, `RawJdbcDemo` |
| **Created by** | Your code, with `new` | The Spring container, at startup |
| **Annotations** | None | `@Service`, `@Repository`, `@Component` |
| **Lifetime** | Created when needed, thrown away after use | One shared instance (singleton) for the whole run |
| **Dependencies** | None — a plain data holder | Injected by Spring (constructor injection), e.g. a service receives its repository |
| **Job** | Carry data (fields + getters/setters) | Do work (rules, SQL, menu) |

## Database

H2 embedded, in-memory (`jdbc:h2:mem:hospitaldb`), reached through
standard JDBC — the same JDBC code works against MySQL by changing
only the driver dependency and the URL in `application.properties`.
Tables: `patient`, `doctor`, `appointment` (3NF; foreign keys with
`ON DELETE CASCADE`).

## Notes

- Cancelling an appointment deletes it, which frees the slot for
  re-booking. Completed appointments stay in the records.
- Deleting a patient or doctor also deletes their appointments
  (foreign-key cascade) — the menu asks for confirmation first.
