package com.hospital.model;

/**
 * POJO (Plain Old Java Object) for a doctor.
 *
 * <p>Like {@link Patient}, this is a plain data holder with no Spring
 * annotation. Spring never creates Doctor objects; the repository builds
 * them from database rows with a RowMapper, and the menu builds them
 * from keyboard input with {@code new}.</p>
 */
public class Doctor {

    private Long id;
    private String name;
    private String specialization;
    private String phone;
    private double consultFee;

    public Doctor() {
    }

    public Doctor(Long id, String name, String specialization,
                  String phone, double consultFee) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.consultFee = consultFee;
    }

    public Doctor(String name, String specialization, String phone, double consultFee) {
        this(null, name, specialization, phone, consultFee);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getConsultFee() { return consultFee; }
    public void setConsultFee(double consultFee) { this.consultFee = consultFee; }

    @Override
    public String toString() {
        return String.format("Doctor[id=%d, name=%s, specialization=%s, phone=%s, fee=Rs. %.2f]",
                id, name, specialization, phone, consultFee);
    }
}
