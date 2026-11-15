package com.civicbuild.dto.response;

import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.enums.BuildingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ApplicationSummaryResponse {
    private Long id;
    private String applicationNumber;
    private String applicantName;
    private String applicantEmail;
    private String propertyAddress;
    private String wardNumber;
    private String zone;
    private BuildingType buildingType;
    private Integer numberOfFloors;
    private BigDecimal builtUpArea;
    private ApplicationStatus status;
    private BigDecimal calculatedFee;
    private String processInstanceKey;
    private LocalDateTime submissionDate;
    private LocalDateTime createdAt;

    public ApplicationSummaryResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }

    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }

    public String getWardNumber() { return wardNumber; }
    public void setWardNumber(String wardNumber) { this.wardNumber = wardNumber; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public BuildingType getBuildingType() { return buildingType; }
    public void setBuildingType(BuildingType buildingType) { this.buildingType = buildingType; }

    public Integer getNumberOfFloors() { return numberOfFloors; }
    public void setNumberOfFloors(Integer numberOfFloors) { this.numberOfFloors = numberOfFloors; }

    public BigDecimal getBuiltUpArea() { return builtUpArea; }
    public void setBuiltUpArea(BigDecimal builtUpArea) { this.builtUpArea = builtUpArea; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public BigDecimal getCalculatedFee() { return calculatedFee; }
    public void setCalculatedFee(BigDecimal calculatedFee) { this.calculatedFee = calculatedFee; }

    public String getProcessInstanceKey() { return processInstanceKey; }
    public void setProcessInstanceKey(String processInstanceKey) { this.processInstanceKey = processInstanceKey; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
