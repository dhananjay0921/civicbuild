package com.civicbuild.entity;

import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.enums.BuildingType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_number", nullable = false, unique = true, length = 50)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_type", nullable = false, length = 50)
    private BuildingType buildingType;

    @Column(name = "construction_purpose", nullable = false, length = 100)
    private String constructionPurpose;

    @Column(name = "number_of_floors", nullable = false)
    private Integer numberOfFloors;

    @Column(name = "built_up_area", nullable = false, precision = 12, scale = 2)
    private BigDecimal builtUpArea;

    @Column(name = "building_height", nullable = false, precision = 8, scale = 2)
    private BigDecimal buildingHeight;

    @Column(name = "estimated_cost", nullable = false, precision = 14, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "parking_available", nullable = false)
    private Boolean parkingAvailable = true;

    @Column(name = "process_instance_key", length = 100)
    private String processInstanceKey;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "calculated_fee", precision = 12, scale = 2)
    private BigDecimal calculatedFee;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DepartmentReview> departmentReviews = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RevisionRequest> revisionRequests = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowHistory> workflowHistories = new ArrayList<>();

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL)
    private Permit permit;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Application() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public User getApplicant() { return applicant; }
    public void setApplicant(User applicant) { this.applicant = applicant; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public BuildingType getBuildingType() { return buildingType; }
    public void setBuildingType(BuildingType buildingType) { this.buildingType = buildingType; }

    public String getConstructionPurpose() { return constructionPurpose; }
    public void setConstructionPurpose(String constructionPurpose) { this.constructionPurpose = constructionPurpose; }

    public Integer getNumberOfFloors() { return numberOfFloors; }
    public void setNumberOfFloors(Integer numberOfFloors) { this.numberOfFloors = numberOfFloors; }

    public BigDecimal getBuiltUpArea() { return builtUpArea; }
    public void setBuiltUpArea(BigDecimal builtUpArea) { this.builtUpArea = builtUpArea; }

    public BigDecimal getBuildingHeight() { return buildingHeight; }
    public void setBuildingHeight(BigDecimal buildingHeight) { this.buildingHeight = buildingHeight; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public Boolean getParkingAvailable() { return parkingAvailable; }
    public void setParkingAvailable(Boolean parkingAvailable) { this.parkingAvailable = parkingAvailable; }

    public String getProcessInstanceKey() { return processInstanceKey; }
    public void setProcessInstanceKey(String processInstanceKey) { this.processInstanceKey = processInstanceKey; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }

    public LocalDateTime getRejectedDate() { return rejectedDate; }
    public void setRejectedDate(LocalDateTime rejectedDate) { this.rejectedDate = rejectedDate; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public BigDecimal getCalculatedFee() { return calculatedFee; }
    public void setCalculatedFee(BigDecimal calculatedFee) { this.calculatedFee = calculatedFee; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }

    public List<DepartmentReview> getDepartmentReviews() { return departmentReviews; }
    public void setDepartmentReviews(List<DepartmentReview> departmentReviews) { this.departmentReviews = departmentReviews; }

    public List<RevisionRequest> getRevisionRequests() { return revisionRequests; }
    public void setRevisionRequests(List<RevisionRequest> revisionRequests) { this.revisionRequests = revisionRequests; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public List<WorkflowHistory> getWorkflowHistories() { return workflowHistories; }
    public void setWorkflowHistories(List<WorkflowHistory> workflowHistories) { this.workflowHistories = workflowHistories; }

    public Permit getPermit() { return permit; }
    public void setPermit(Permit permit) { this.permit = permit; }
}
