package com.civicbuild.entity;

import com.civicbuild.enums.DepartmentCode;
import com.civicbuild.enums.RevisionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "revision_requests")
public class RevisionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "department_code", nullable = false, length = 50)
    private DepartmentCode departmentCode;

    @Column(name = "officer_remarks", nullable = false, columnDefinition = "TEXT")
    private String officerRemarks;

    @Column(name = "cycle_number", nullable = false)
    private Integer cycleNumber = 1;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RevisionStatus status = RevisionStatus.REQUESTED;

    public RevisionRequest() {}

    public RevisionRequest(Application application, DepartmentCode departmentCode, String officerRemarks, Integer cycleNumber) {
        this.application = application;
        this.departmentCode = departmentCode;
        this.officerRemarks = officerRemarks;
        this.cycleNumber = cycleNumber;
        this.status = RevisionStatus.REQUESTED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public DepartmentCode getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(DepartmentCode departmentCode) { this.departmentCode = departmentCode; }

    public String getOfficerRemarks() { return officerRemarks; }
    public void setOfficerRemarks(String officerRemarks) { this.officerRemarks = officerRemarks; }

    public Integer getCycleNumber() { return cycleNumber; }
    public void setCycleNumber(Integer cycleNumber) { this.cycleNumber = cycleNumber; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    public RevisionStatus getStatus() { return status; }
    public void setStatus(RevisionStatus status) { this.status = status; }
}
