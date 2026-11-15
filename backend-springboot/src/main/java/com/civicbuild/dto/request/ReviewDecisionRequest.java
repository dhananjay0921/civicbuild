package com.civicbuild.dto.request;

import com.civicbuild.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewDecisionRequest {
    @NotNull(message = "Review decision is required")
    private ReviewStatus status;

    @NotBlank(message = "Review remarks are mandatory")
    private String remarks;

    public ReviewDecisionRequest() {}

    public ReviewDecisionRequest(ReviewStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
