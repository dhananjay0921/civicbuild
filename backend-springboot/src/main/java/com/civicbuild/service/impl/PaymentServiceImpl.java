package com.civicbuild.service.impl;

import com.civicbuild.entity.Application;
import com.civicbuild.entity.Payment;
import com.civicbuild.entity.User;
import com.civicbuild.entity.WorkflowHistory;
import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.enums.PaymentMethod;
import com.civicbuild.enums.PaymentStatus;
import com.civicbuild.enums.UserRole;
import com.civicbuild.exception.BadRequestException;
import com.civicbuild.exception.ResourceNotFoundException;
import com.civicbuild.exception.UnauthorizedException;
import com.civicbuild.repository.ApplicationRepository;
import com.civicbuild.repository.PaymentRepository;
import com.civicbuild.repository.WorkflowHistoryRepository;
import com.civicbuild.service.PaymentService;
import com.civicbuild.service.WorkflowOrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WorkflowHistoryRepository workflowHistoryRepository;

    @Autowired
    private WorkflowOrchestrationService workflowOrchestrationService;

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentForApplication(Long applicationId, User user) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        validateAccess(app, user);

        List<Payment> payments = paymentRepository.findByApplicationId(applicationId);
        if (!payments.isEmpty()) {
            return payments.get(payments.size() - 1);
        }

        // Return empty or virtual payment request if calculated fee exists
        BigDecimal fee = app.getCalculatedFee() != null ? app.getCalculatedFee() : BigDecimal.valueOf(5000.0);
        String txRef = "PAY-" + System.currentTimeMillis() + "-" + (1000 + new Random().nextInt(9000));
        Payment payment = new Payment(app, fee, txRef, PaymentMethod.UPI);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment initiatePayment(Long applicationId, PaymentMethod method, User user) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        validateAccess(app, user);

        BigDecimal fee = app.getCalculatedFee() != null ? app.getCalculatedFee() : BigDecimal.valueOf(5000.0);
        String txRef = "PAY-" + System.currentTimeMillis() + "-" + (1000 + new Random().nextInt(9000));

        Payment payment = new Payment(app, fee, txRef, method);
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        app.setStatus(ApplicationStatus.PAYMENT_PENDING);
        applicationRepository.save(app);

        workflowHistoryRepository.save(new WorkflowHistory(
                app, "PAYMENT", "PAYMENT_INITIATED", user.getUsername(),
                "Online payment session initialized with method " + method.name() + " and Ref: " + txRef,
                app.getProcessInstanceKey()
        ));

        return payment;
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public Payment confirmPayment(Long applicationId, String paymentReference, Boolean mockSuccess, User user) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        validateAccess(app, user);

        Payment payment = paymentRepository.findByTransactionRef(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ref: " + paymentReference));

        if (mockSuccess != null && !mockSuccess) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setGatewayResponse("{\"status\":\"DECLINED\",\"reason\":\"Simulated Card Declined by Gateway\",\"code\":\"ERR_402\"}");
            paymentRepository.save(payment);

            workflowHistoryRepository.save(new WorkflowHistory(
                    app, "PAYMENT", "PAYMENT_FAILED", user.getUsername(),
                    "Payment transaction failed for ref: " + paymentReference,
                    app.getProcessInstanceKey()
            ));

            throw new BadRequestException("Payment failed: Simulated mock payment gateway rejected the transaction. Please try again.");
        }

        // Confirmed Success
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setGatewayResponse("{\"status\":\"SUCCESS\",\"authCode\":\"MOCK_AUTH_9921\",\"timestamp\":\"" + LocalDateTime.now() + "\"}");
        payment = paymentRepository.save(payment);

        workflowOrchestrationService.triggerPaymentConfirmed(applicationId);

        return payment;
    }

    private void validateAccess(Application application, User user) {
        if (user.getRole().getName() == UserRole.ROLE_ADMIN ||
            user.getRole().getName() == UserRole.ROLE_SUPER_ADMIN ||
            user.getRole().getName() == UserRole.ROLE_OFFICER) {
            return;
        }
        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to view or make payments for this application");
        }
    }
}
