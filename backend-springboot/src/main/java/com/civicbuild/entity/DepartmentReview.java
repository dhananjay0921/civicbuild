package com.civicbuild.entity;

import com.civicbuild.enums.DepartmentCode;
import com.civicbuild.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "department_reviews")
public class DepartmentReview {

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "officer_id")
    private User officer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public DepartmentReview() {}

    public DepartmentReview(Application application, DepartmentCode departmentCode, LocalDateTime dueDate) {
        this.application = application;
        this.departmentCode = departmentCode;
        this.dueDate = dueDate;
        this.status = ReviewStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public DepartmentCode getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(DepartmentCode departmentCode) { this.departmentCode = departmentCode; }

    public User getOfficer() { return officer; }
    public void setOfficer(User officer) { this.officer = officer; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
