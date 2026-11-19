-- Hospital Appointment and Patient Record Management System
-- Schema: 3 tables in 3NF. Runs on H2 (embedded) through standard JDBC.

CREATE TABLE IF NOT EXISTS patient (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    age     INT NOT NULL CHECK (age > 0 AND age < 130),
    gender  VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female', 'Other')),
    phone   VARCHAR(15) NOT NULL UNIQUE,
    address VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS doctor (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    specialization VARCHAR(60) NOT NULL,
    phone          VARCHAR(15) NOT NULL UNIQUE,
    consult_fee    DECIMAL(8,2) NOT NULL CHECK (consult_fee >= 0)
);

CREATE TABLE IF NOT EXISTS appointment (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id       BIGINT NOT NULL,
    doctor_id        BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    time_slot        VARCHAR(5) NOT NULL,
    reason           VARCHAR(200),
    status           VARCHAR(15) NOT NULL DEFAULT 'BOOKED'
                     CHECK (status IN ('BOOKED', 'COMPLETED')),
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id)  REFERENCES doctor(id)  ON DELETE CASCADE,
    -- The database-level guard against double booking: the same doctor
    -- cannot hold two appointments in the same slot on the same day.
    -- The service layer checks first and reports a friendly message;
    -- this constraint makes a duplicate impossible even if the check
    -- were ever bypassed.
    CONSTRAINT uq_doctor_slot UNIQUE (doctor_id, appointment_date, time_slot)
);
