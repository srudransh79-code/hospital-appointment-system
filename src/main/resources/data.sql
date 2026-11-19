-- Sample data, loaded fresh on every run (in-memory database).

INSERT INTO doctor (name, specialization, phone, consult_fee) VALUES
('Dr. Anil Sharma',  'Cardiology',        '9811111111', 800.00),
('Dr. Neha Verma',   'Orthopedics',       '9822222222', 700.00),
('Dr. Rajesh Iyer',  'Pediatrics',        '9833333333', 600.00),
('Dr. Sunita Rao',   'General Medicine',  '9844444444', 500.00);

INSERT INTO patient (name, age, gender, phone, address) VALUES
('Aarav Gupta',  21, 'Male',   '9810000001', 'Sector 17, Chandigarh'),
('Priya Nair',   34, 'Female', '9810000002', 'Model Town, Ludhiana'),
('Rohit Kumar',  45, 'Male',   '9810000003', 'Civil Lines, Delhi'),
('Sneha Patel',  29, 'Female', '9810000004', 'Satellite, Ahmedabad'),
('Vikram Singh', 52, 'Male',   '9810000005', 'Banjara Hills, Hyderabad');

-- 3 sample appointments on 2026-11-16 (a Monday).
INSERT INTO appointment (patient_id, doctor_id, appointment_date, time_slot, reason, status) VALUES
(1, 1, DATE '2026-11-16', '09:00', 'Chest pain checkup', 'BOOKED'),
(2, 2, DATE '2026-11-16', '10:00', 'Knee pain',          'BOOKED'),
(3, 4, DATE '2026-11-16', '11:30', 'Fever and cold',     'BOOKED');
