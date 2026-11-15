package com.civicbuild.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "survey_number", nullable = false, length = 50)
    private String surveyNumber;

    @Column(name = "ward_number", nullable = false, length = 20)
    private String wardNumber;

    @Column(nullable = false, length = 50)
    private String zone;

    @Column(name = "plot_area", nullable = false, precision = 12, scale = 2)
    private BigDecimal plotArea;

    @Column(name = "ownership_type", nullable = false, length = 50)
    private String ownershipType;

    public Property() {}

    public Property(String address, String surveyNumber, String wardNumber, String zone, BigDecimal plotArea, String ownershipType) {
        this.address = address;
        this.surveyNumber = surveyNumber;
        this.wardNumber = wardNumber;
        this.zone = zone;
        this.plotArea = plotArea;
        this.ownershipType = ownershipType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
}
