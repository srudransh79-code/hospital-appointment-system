package com.hospital.model;

/**
 * POJO (Plain Old Java Object) for a patient.
 *
 * <p>A POJO is an ordinary Java class: private fields (encapsulation),
 * a no-arg constructor, a full constructor, and getters/setters. It has
 * NO Spring annotation, is NOT managed by Spring, and is created with
 * plain {@code new Patient(...)} wherever it is needed. Contrast with
 * the service and repository classes, which are Spring-managed beans:
 * Spring creates them once and injects them with @Autowired.</p>
 */
public class Patient {

    private Long id;
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String address;

    public Patient() {
    }

    public Patient(Long id, String name, int age, String gender,
                   String phone, String address) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
    }

    public Patient(String name, int age, String gender, String phone, String address) {
        this(null, name, age, gender, phone, address);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return String.format("Patient[id=%d, name=%s, age=%d, gender=%s, phone=%s, address=%s]",
                id, name, age, gender, phone, address);
    }
}
