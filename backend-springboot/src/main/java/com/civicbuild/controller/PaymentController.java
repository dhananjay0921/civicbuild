package com.civicbuild.controller;

import com.civicbuild.dto.request.PaymentConfirmRequest;
import com.civicbuild.dto.request.PaymentInitiateRequest;
import com.civicbuild.dto.response.ApiResponse;
import com.civicbuild.entity.Payment;
import com.civicbuild.entity.User;
import com.civicbuild.enums.PaymentMethod;
import com.civicbuild.service.AuthService;
import com.civicbuild.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuthService authService;

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<Payment>> getPayment(@PathVariable Long applicationId) {
        User currentUser = authService.getCurrentUser();
        Payment payment = paymentService.getPaymentForApplication(applicationId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment details retrieved", payment));
    }

    @PostMapping("/{applicationId}/initiate")
    public ResponseEntity<ApiResponse<Payment>> initiatePayment(
            @PathVariable Long applicationId,
            @Valid @RequestBody PaymentInitiateRequest request) {
        User currentUser = authService.getCurrentUser();
        Payment payment = paymentService.initiatePayment(applicationId, request.getPaymentMethod(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("Payment session initiated", payment));
    }

    @PostMapping("/{applicationId}/confirm")
    public ResponseEntity<ApiResponse<Payment>> confirmPayment(
            @PathVariable Long applicationId,
            @Valid @RequestBody PaymentConfirmRequest request) {
        User currentUser = authService.getCurrentUser();
        Payment payment = paymentService.confirmPayment(
                applicationId,
                request.getPaymentReference(),
                request.getMockSuccess() != null ? request.getMockSuccess() : true,
                currentUser
        );
        return ResponseEntity.ok(ApiResponse.success("Payment successfully verified by gateway", payment));
    }
}
