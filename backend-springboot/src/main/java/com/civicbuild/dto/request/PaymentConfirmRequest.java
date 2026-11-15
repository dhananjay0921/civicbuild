package com.civicbuild.dto.request;

import jakarta.validation.constraints.NotBlank;

public class PaymentConfirmRequest {
    @NotBlank(message = "Payment reference is required")
    private String paymentReference;

    private Boolean mockSuccess = true;

    public PaymentConfirmRequest() {}

    public PaymentConfirmRequest(String paymentReference, Boolean mockSuccess) {
        this.paymentReference = paymentReference;
        this.mockSuccess = mockSuccess;
    }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public Boolean getMockSuccess() { return mockSuccess; }
    public void setMockSuccess(Boolean mockSuccess) { this.mockSuccess = mockSuccess; }
}
