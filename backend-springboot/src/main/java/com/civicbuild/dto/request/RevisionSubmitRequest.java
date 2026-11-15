package com.civicbuild.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RevisionSubmitRequest {
    @NotBlank(message = "Response remarks describing document corrections are required")
    private String remarks;

    public RevisionSubmitRequest() {}

    public RevisionSubmitRequest(String remarks) {
        this.remarks = remarks;
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
