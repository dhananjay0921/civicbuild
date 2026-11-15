package com.civicbuild.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "permits")
public class Permit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permit_number", nullable = false, unique = true, length = 50)
    private String permitNumber;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "qr_code_data", nullable = false, columnDefinition = "TEXT")
    private String qrCodeData;

    @Column(name = "pdf_path", nullable = false, length = 500)
    private String pdfPath;

    @Column(name = "issued_by", nullable = false, length = 100)
    private String issuedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Permit() {}

    public Permit(String permitNumber, Application application, LocalDateTime issueDate, LocalDateTime expiryDate, String qrCodeData, String pdfPath, String issuedBy) {
        this.permitNumber = permitNumber;
        this.application = application;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.qrCodeData = qrCodeData;
        this.pdfPath = pdfPath;
        this.issuedBy = issuedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPermitNumber() { return permitNumber; }
    public void setPermitNumber(String permitNumber) { this.permitNumber = permitNumber; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
