package com.civicbuild.worker;

import com.civicbuild.entity.*;
import com.civicbuild.enums.*;
import com.civicbuild.repository.*;
import com.civicbuild.service.NotificationService;
import com.civicbuild.service.PermitService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class CivicBuildJobWorkers {

    private static final Logger log = LoggerFactory.getLogger(CivicBuildJobWorkers.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DepartmentReviewRepository departmentReviewRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WorkflowHistoryRepository workflowHistoryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PermitService permitService;

    @Value("${civicbuild.fees.residential-base:5000.0}")
    private double resBase;
    @Value("${civicbuild.fees.residential-per-sqft:2.5}")
    private double resPerSqft;
    @Value("${civicbuild.fees.residential-floor-surcharge:1500.0}")
    private double resFloorSurcharge;

    @Value("${civicbuild.fees.commercial-base:15000.0}")
    private double commBase;
    @Value("${civicbuild.fees.commercial-per-sqft:6.0}")
    private double commPerSqft;
    @Value("${civicbuild.fees.commercial-floor-surcharge:3500.0}")
    private double commFloorSurcharge;

    @Value("${civicbuild.fees.industrial-base:25000.0}")
    private double indBase;
    @Value("${civicbuild.fees.industrial-per-sqft:8.0}")
    private double indPerSqft;
    @Value("${civicbuild.fees.industrial-floor-surcharge:5000.0}")
    private double indFloorSurcharge;

    @Value("${civicbuild.fees.institutional-base:10000.0}")
    private double instBase;
    @Value("${civicbuild.fees.institutional-per-sqft:4.0}")
    private double instPerSqft;
    @Value("${civicbuild.fees.institutional-floor-surcharge:2000.0}")
    private double instFloorSurcharge;

    // WORKER 1: validate-application
    @JobWorker(type = "validate-application", autoComplete = true)
    @Transactional
    public Map<String, Object> handleValidateApplication(@Variable Long applicationId) {
        log.info("[JobWorker: validate-application] Validating application ID: {}", applicationId);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        boolean isValid = app.getProperty() != null &&
                app.getBuildingType() != null &&
                app.getNumberOfFloors() != null && app.getNumberOfFloors() > 0 &&
                app.getBuiltUpArea() != null && app.getBuiltUpArea().compareTo(BigDecimal.ZERO) > 0;

        app.setStatus(ApplicationStatus.UNDER_DOCUMENT_VERIFICATION);
        applicationRepository.save(app);

        workflowHistoryRepository.save(new WorkflowHistory(
                app, "VALIDATION", "APPLICATION_VALIDATED", "SYSTEM",
                "Application schema and statutory boundaries successfully validated",
                app.getProcessInstanceKey()
        ));

        Map<String, Object> output = new HashMap<>();
        output.put("isValid", isValid);
        return output;
    }

    // WORKER 2: verify-land-registry
    @JobWorker(type = "verify-land-registry", autoComplete = true)
    @Transactional
    public Map<String, Object> handleVerifyLandRegistry(@Variable Long applicationId, @Variable(name = "surveyNumber") String surveyNumber) {
        log.info("[JobWorker: verify-land-registry] Verifying land records for app ID: {}, surveyNumber: {}", applicationId, surveyNumber);
        Application app = applicationRepository.findById(applicationId).orElse(null);

        boolean isTitleClear = surveyNumber != null && !surveyNumber.trim().isEmpty();
        if (app != null) {
            workflowHistoryRepository.save(new WorkflowHistory(
                    app, "LAND_REGISTRY", "TITLE_SEARCH_VERIFIED", "MOCK_LAND_CONNECTOR",
                    "Municipal land registry confirmed title clearance for survey " + surveyNumber,
                    app.getProcessInstanceKey()
            ));
        }

        Map<String, Object> output = new HashMap<>();
        output.put("isTitleClear", isTitleClear);
        return output;
    }

    // WORKER 3: verify-document (Sequential Multi-Instance Task)
    @JobWorker(type = "verify-document", autoComplete = true)
    @Transactional
    public Map<String, Object> handleVerifyDocument(@Variable Long applicationId, @Variable(name = "currentDocument") Object currentDocument) {
        log.info("[JobWorker: verify-document] Checking document for app ID: {}, doc: {}", applicationId, currentDocument);

        String docTypeStr = "UNKNOWN";
        if (currentDocument instanceof Map<?, ?> docMap) {
            docTypeStr = String.valueOf(docMap.get("documentType"));
        } else if (currentDocument != null) {
            docTypeStr = currentDocument.toString();
        }

        List<Document> docs = documentRepository.findByApplicationId(applicationId);
        boolean found = false;
        for (Document d : docs) {
            if (d.getDocumentType().name().equalsIgnoreCase(docTypeStr)) {
                d.setVerificationStatus(VerificationStatus.VERIFIED);
                documentRepository.save(d);
                found = true;
                break;
            }
        }

        // If mandatory document is missing, throw ZeebeBpmnError to trigger BPMN Error Boundary Event!
        if (!found && ("PROPERTY_OWNERSHIP".equalsIgnoreCase(docTypeStr) || "BUILDING_BLUEPRINT".equalsIgnoreCase(docTypeStr))) {
            log.warn("Mandatory document {} missing for application ID {}", docTypeStr, applicationId);
            throw new ZeebeBpmnError("MISSING_CRITICAL_DOCUMENT", "Mandatory statutory document check failed: " + docTypeStr);
        }

        Map<String, Object> output = new HashMap<>();
        output.put("verificationResult", "VERIFIED");
        return output;
    }

    // WORKER 4: calculate-municipal-fee
    @JobWorker(type = "calculate-municipal-fee", autoComplete = true)
    @Transactional
    public Map<String, Object> handleCalculateMunicipalFee(
            @Variable Long applicationId,
            @Variable String buildingType,
            @Variable Double builtUpArea,
            @Variable Integer numberOfFloors) {

        log.info("[JobWorker: calculate-municipal-fee] App ID: {}, Type: {}, Area: {}, Floors: {}",
                applicationId, buildingType, builtUpArea, numberOfFloors);

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        double base = resBase;
        double perSqft = resPerSqft;
        double floorRate = resFloorSurcharge;

        if (BuildingType.COMMERCIAL.name().equalsIgnoreCase(buildingType)) {
            base = commBase; perSqft = commPerSqft; floorRate = commFloorSurcharge;
        } else if (BuildingType.INDUSTRIAL.name().equalsIgnoreCase(buildingType)) {
            base = indBase; perSqft = indPerSqft; floorRate = indFloorSurcharge;
        } else if (BuildingType.INSTITUTIONAL.name().equalsIgnoreCase(buildingType)) {
            base = instBase; perSqft = instPerSqft; floorRate = instFloorSurcharge;
        }

        double areaCost = (builtUpArea != null ? builtUpArea : 100.0) * perSqft;
        double floorCost = (numberOfFloors != null ? numberOfFloors : 1) * floorRate;
        double total = base + areaCost + floorCost;

        BigDecimal feeBigDecimal = BigDecimal.valueOf(total);
        app.setCalculatedFee(feeBigDecimal);
        app.setStatus(ApplicationStatus.FEE_PENDING);
        applicationRepository.save(app);

        workflowHistoryRepository.save(new WorkflowHistory(
                app, "FEE_CALCULATION", "FEES_COMPUTED", "SYSTEM",
                String.format("Statutory fees computed: Base=%.2f, Area=%.2f, Floor=%.2f, Total=%.2f INR",
                        base, areaCost, floorCost, total),
                app.getProcessInstanceKey()
        ));

        Map<String, Object> output = new HashMap<>();
        output.put("totalFee", total);
        output.put("feeAmount", total);
        output.put("feeBreakdown", String.format("Base: %.2f, Area Surcharge: %.2f, Floor Surcharge: %.2f", base, areaCost, floorCost));
        return output;
    }

    // WORKER 5: create-payment-request
    @JobWorker(type = "create-payment-request", autoComplete = true)
    @Transactional
    public Map<String, Object> handleCreatePaymentRequest(@Variable Long applicationId, @Variable Double amount) {
        log.info("[JobWorker: create-payment-request] App ID: {}, Amount: {}", applicationId, amount);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        String txRef = "PAY-" + System.currentTimeMillis() + "-" + (1000 + new Random().nextInt(9000));
        BigDecimal payAmount = amount != null ? BigDecimal.valueOf(amount) :
                (app.getCalculatedFee() != null ? app.getCalculatedFee() : BigDecimal.valueOf(5000.0));

        Payment payment = new Payment(app, payAmount, txRef, PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        app.setStatus(ApplicationStatus.PAYMENT_PENDING);
        applicationRepository.save(app);

        workflowHistoryRepository.save(new WorkflowHistory(
                app, "PAYMENT", "PAYMENT_REQUEST_CREATED", "SYSTEM",
                "Payment invoice generated with reference " + txRef,
                app.getProcessInstanceKey()
        ));

        Map<String, Object> output = new HashMap<>();
        output.put("paymentReference", txRef);
        return output;
    }

    // WORKER 6: confirm-payment
    @JobWorker(type = "confirm-payment", autoComplete = true)
    @Transactional
    public Map<String, Object> handleConfirmPayment(@Variable Long applicationId, @Variable String paymentReference) {
        log.info("[JobWorker: confirm-payment] App ID: {}, Ref: {}", applicationId, paymentReference);
        Application app = applicationRepository.findById(applicationId).orElse(null);

        Optional<Payment> paymentOpt = paymentRepository.findByTransactionRef(paymentReference);
        String paymentStatus = "SUCCESS";
        if (paymentOpt.isPresent()) {
            Payment p = paymentOpt.get();
            if (p.getStatus() == PaymentStatus.FAILED) {
                paymentStatus = "FAILED";
            } else {
                p.setStatus(PaymentStatus.SUCCESS);
                p.setPaymentDate(LocalDateTime.now());
                p.setGatewayResponse("{\"status\":\"CONFIRMED\",\"code\":\"MOCK_200\",\"txTime\":\"" + LocalDateTime.now() + "\"}");
                paymentRepository.save(p);
            }
        }

        if (app != null && "SUCCESS".equals(paymentStatus)) {
            workflowHistoryRepository.save(new WorkflowHistory(
                    app, "PAYMENT", "PAYMENT_CONFIRMED", "MOCK_PAYMENT_GATEWAY",
                    "Payment confirmed for ref " + paymentReference,
                    app.getProcessInstanceKey()
            ));
        }

        Map<String, Object> output = new HashMap<>();
        output.put("paymentStatus", paymentStatus);
        return output;
    }

    // WORKER 7: generate-permit
    @JobWorker(type = "generate-permit", autoComplete = true)
    @Transactional
    public Map<String, Object> handleGeneratePermit(@Variable Long applicationId) {
        log.info("[JobWorker: generate-permit] Generating official permit for App ID: {}", applicationId);
        Permit permit = permitService.createPermitRecord(applicationId);

        Map<String, Object> output = new HashMap<>();
        output.put("permitNumber", permit.getPermitNumber());
        output.put("pdfPath", permit.getPdfPath());
        return output;
    }

    // WORKER 8: send-notification
    @JobWorker(type = "send-notification", autoComplete = true)
    @Transactional
    public Map<String, Object> handleSendNotification(
            @Variable Long applicationId,
            @Variable String notificationType,
            @Variable(name = "recipientEmail") String recipientEmail,
            @Variable(name = "permitNumber") String permitNumber) {

        log.info("[JobWorker: send-notification] App ID: {}, Type: {}, Recipient: {}", applicationId, notificationType, recipientEmail);
        NotificationType type = NotificationType.SUBMITTED;
        try {
            if (notificationType != null) {
                type = NotificationType.valueOf(notificationType);
            }
        } catch (Exception ignored) {}

        String title = "CivicBuild Update: " + type.name();
        String message = "Your building permit application status has progressed to " + type.name() +
                (permitNumber != null ? ". Issued Permit Number: " + permitNumber : "");

        notificationService.createNotification(recipientEmail, title, message, type);

        Map<String, Object> output = new HashMap<>();
        output.put("notificationSent", true);
        return output;
    }

    // WORKER 9: update-application-status
    @JobWorker(type = "update-application-status", autoComplete = true)
    @Transactional
    public Map<String, Object> handleUpdateApplicationStatus(
            @Variable Long applicationId,
            @Variable String status,
            @Variable(name = "reason") String reason) {

        log.info("[JobWorker: update-application-status] App ID: {}, Target Status: {}, Reason: {}", applicationId, status, reason);
        Application app = applicationRepository.findById(applicationId).orElse(null);
        if (app != null) {
            try {
                ApplicationStatus target = ApplicationStatus.valueOf(status);
                app.setStatus(target);
                if (target == ApplicationStatus.REJECTED) {
                    app.setRejectedDate(LocalDateTime.now());
                    app.setRejectionReason(reason);
                } else if (target == ApplicationStatus.APPROVED) {
                    app.setApprovedDate(LocalDateTime.now());
                }
                applicationRepository.save(app);

                workflowHistoryRepository.save(new WorkflowHistory(
                        app, "STATUS_UPDATE", "STATUS_CHANGED_" + status, "WORKFLOW_ENGINE",
                        reason != null ? reason : "Status transitioned to " + status,
                        app.getProcessInstanceKey()
                ));
            } catch (Exception e) {
                log.error("Failed to map application status: {}", status, e);
            }
        }

        Map<String, Object> output = new HashMap<>();
        output.put("statusUpdated", true);
        return output;
    }

    // WORKER 10: handle-review-timeout
    @JobWorker(type = "handle-review-timeout", autoComplete = true)
    @Transactional
    public Map<String, Object> handleReviewTimeout(
            @Variable Long applicationId,
            @Variable(name = "overdueDepartment") String overdueDepartment) {

        log.warn("[JobWorker: handle-review-timeout] Overdue SLA breach escalated for App ID: {}, Department: {}",
                applicationId, overdueDepartment);

        Application app = applicationRepository.findById(applicationId).orElse(null);
        if (app != null) {
            workflowHistoryRepository.save(new WorkflowHistory(
                    app, "SLA_BREACH", "REVIEW_TIMEOUT_ESCALATED", "TIMER_BOUNDARY_EVENT",
                    "Overdue review escalation dispatched to Municipal Administrator for " + overdueDepartment,
                    app.getProcessInstanceKey()
            ));

            auditLogRepository.save(new AuditLog(
                    null, "SLA_TIMEOUT_ESCALATION", "APPLICATION", String.valueOf(applicationId),
                    "127.0.0.1", "Department review timed out: " + overdueDepartment
            ));
        }

        Map<String, Object> output = new HashMap<>();
        output.put("escalationHandled", true);
        return output;
    }
}
