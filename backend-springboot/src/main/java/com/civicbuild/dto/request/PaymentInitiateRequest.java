package com.civicbuild.dto.request;

import com.civicbuild.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class PaymentInitiateRequest {
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    public PaymentInitiateRequest() {}

    public PaymentInitiateRequest(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
