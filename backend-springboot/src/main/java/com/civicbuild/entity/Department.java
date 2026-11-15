package com.civicbuild.entity;

import com.civicbuild.enums.DepartmentCode;
import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private DepartmentCode code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_email", nullable = false, length = 100)
    private String contactEmail;

    @Column(name = "sla_days", nullable = false)
    private Integer slaDays = 7;

    public Department() {}

    public Department(DepartmentCode code, String name, String description, String contactEmail, Integer slaDays) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.contactEmail = contactEmail;
        this.slaDays = slaDays;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DepartmentCode getCode() { return code; }
    public void setCode(DepartmentCode code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public Integer getSlaDays() { return slaDays; }
    public void setSlaDays(Integer slaDays) { this.slaDays = slaDays; }
}
