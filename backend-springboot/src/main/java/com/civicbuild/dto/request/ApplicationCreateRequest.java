package com.civicbuild.dto.request;

import com.civicbuild.enums.BuildingType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ApplicationCreateRequest {

    // Property Information
    @NotBlank(message = "Property address is required")
    private String address;

    @NotBlank(message = "Survey number is required")
    private String surveyNumber;

    @NotBlank(message = "Ward number is required")
    private String wardNumber;

    @NotBlank(message = "Zone is required")
    private String zone;

    @NotNull(message = "Plot area is required")
    @DecimalMin(value = "100.0", message = "Plot area must be at least 100 sq.ft")
    private BigDecimal plotArea;

    @NotBlank(message = "Ownership type is required")
    private String ownershipType;

    // Construction Details
    @NotNull(message = "Building type is required")
    private BuildingType buildingType;

    @NotBlank(message = "Construction purpose is required")
    private String constructionPurpose;

    @NotNull(message = "Number of floors is required")
    @Min(value = 1, message = "Building must have at least 1 floor")
    @Max(value = 100, message = "Floor count exceeds statutory limit")
    private Integer numberOfFloors;

    @NotNull(message = "Built-up area is required")
    @DecimalMin(value = "50.0", message = "Built up area must be positive")
    private BigDecimal builtUpArea;

    @NotNull(message = "Building height is required")
    @DecimalMin(value = "2.5", message = "Height must be at least 2.5 meters")
    private BigDecimal buildingHeight;

    @NotNull(message = "Estimated cost is required")
    @DecimalMin(value = "10000.0", message = "Estimated cost must be realistic")
    private BigDecimal estimatedCost;

    private Boolean parkingAvailable = true;

    public ApplicationCreateRequest() {}

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getSurveyNumber() { return surveyNumber; }
    public void setSurveyNumber(String surveyNumber) { this.surveyNumber = surveyNumber; }

    public String getWardNumber() { return wardNumber; }
    public void setWardNumber(String wardNumber) { this.wardNumber = wardNumber; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public BigDecimal getPlotArea() { return plotArea; }
    public void setPlotArea(BigDecimal plotArea) { this.plotArea = plotArea; }

    public String getOwnershipType() { return ownershipType; }
    public void setOwnershipType(String ownershipType) { this.ownershipType = ownershipType; }

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
}
