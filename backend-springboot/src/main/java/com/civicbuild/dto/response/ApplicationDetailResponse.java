package com.civicbuild.dto.response;

import com.civicbuild.entity.*;
import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.enums.BuildingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ApplicationDetailResponse {
    private Long id;
    private String applicationNumber;
    private User applicant;
    private Property property;
    private ApplicationStatus status;
    private BuildingType buildingType;
    private String constructionPurpose;
    private Integer numberOfFloors;
    private BigDecimal builtUpArea;
    private BigDecimal buildingHeight;
    private BigDecimal estimatedCost;
    private Boolean parkingAvailable;
    private String processInstanceKey;
    private LocalDateTime submissionDate;
    private LocalDateTime approvedDate;
    private LocalDateTime rejectedDate;
    private String rejectionReason;
    private BigDecimal calculatedFee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Document> documents;
    private List<DepartmentReview> departmentReviews;
    private List<RevisionRequest> revisionRequests;
    private List<Payment> payments;
    private List<WorkflowHistory> workflowHistories;
    private Permit permit;

    public ApplicationDetailResponse() {}

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
