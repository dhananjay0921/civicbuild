package com.civicbuild.service.impl;

import com.civicbuild.entity.*;
import com.civicbuild.enums.*;
import com.civicbuild.repository.*;
import com.civicbuild.service.NotificationService;
import com.civicbuild.service.WorkflowOrchestrationService;
import com.civicbuild.worker.CivicBuildJobWorkers;
import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class WorkflowOrchestrationServiceImpl implements WorkflowOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowOrchestrationServiceImpl.class);

    @Autowired(required = false)
    private ZeebeClient zeebeClient;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentReviewRepository departmentReviewRepository;

    @Autowired
    private RevisionRequestRepository revisionRequestRepository;

    @Autowired
    private WorkflowHistoryRepository workflowHistoryRepository;

    @Autowired
    private CivicBuildJobWorkers jobWorkers;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public String startProcessInstance(Application application) {
        log.info("Starting workflow orchestration for application ID: {}", application.getId());
        String processInstanceKey = "CB-PROC-" + System.currentTimeMillis();

        Map<String, Object> variables = new HashMap<>();
        variables.put("applicationId", application.getId());
        variables.put("applicationNumber", application.getApplicationNumber());
        variables.put("buildingType", application.getBuildingType().name());
        variables.put("plotArea", application.getProperty().getPlotArea().doubleValue());
        variables.put("builtUpArea", application.getBuiltUpArea().doubleValue());
        variables.put("numberOfFloors", application.getNumberOfFloors());
        variables.put("surveyNumber", application.getProperty().getSurveyNumber());
        variables.put("recipientEmail", application.getApplicant().getEmail());
        variables.put("departmentList", List.of("FIRE", "WATER", "ZONING", "ENVIRONMENTAL"));
        variables.put("revisionCount", 0);
        variables.put("maxRevisions", 3);

        if (zeebeClient != null) {
            try {
                var event = zeebeClient.newCreateInstanceCommand()
                        .bpmnProcessId("BuildingPermitApprovalProcess")
                        .latestVersion()
                        .variables(variables)
                        .send()
                        .join();
                processInstanceKey = String.valueOf(event.getProcessInstanceKey());
                log.info("Zeebe process instance spawned with key: {}", processInstanceKey);
            } catch (Exception e) {
                log.warn("Zeebe broker connection unavailable, fallback to synchronized orchestrator: {}", e.getMessage());
            }
        }

        application.setProcessInstanceKey(processInstanceKey);
        applicationRepository.save(application);

        // Step 1: Validate Application
        jobWorkers.handleValidateApplication(application.getId());

        // Step 2: Land Registry Title Search
        jobWorkers.handleVerifyLandRegistry(application.getId(), application.getProperty().getSurveyNumber());

        // Step 3: Sequential Document Verification (Checklist)
        List<Document> docs = application.getDocuments();
        for (Document d : docs) {
            jobWorkers.handleVerifyDocument(application.getId(), d.getDocumentType().name());
        }

        // Step 4: Spawn 4 Parallel Department Reviews (FIRE, WATER, ZONING, ENVIRONMENTAL)
        DepartmentCode[] depts = DepartmentCode.values();
        for (DepartmentCode code : depts) {
            Optional<DepartmentReview> existing = departmentReviewRepository.findByApplicationIdAndDepartmentCode(application.getId(), code);
            if (existing.isEmpty()) {
                DepartmentReview review = new DepartmentReview(application, code, LocalDateTime.now().plusDays(7));
                review.setStatus(ReviewStatus.PENDING);
                departmentReviewRepository.save(review);
            }
        }

        application.setStatus(ApplicationStatus.UNDER_DEPARTMENT_REVIEW);
        applicationRepository.save(application);

        workflowHistoryRepository.save(new WorkflowHistory(
                application, "DEPARTMENT_REVIEW", "PARALLEL_REVIEWS_SPAWNED", "WORKFLOW_ENGINE",
                "Four parallel review instances created for Fire, Water, Zoning, and Environmental departments",
                processInstanceKey
        ));

        notificationService.createNotification(
                application.getApplicant().getEmail(),
                "Application Under Department Review",
                "Your application " + application.getApplicationNumber() + " is currently under review across 4 municipal departments.",
                NotificationType.UNDER_REVIEW
        );

        return processInstanceKey;
    }

    @Override
    @Transactional
    public void completeUserTask(String taskId, Map<String, Object> variables) {
        log.info("Completing task {} with variables {}", taskId, variables);
        if (zeebeClient != null) {
            try {
                long jobKey = Long.parseLong(taskId);
                zeebeClient.newCompleteCommand(jobKey).variables(variables).send().join();
            } catch (Exception e) {
                log.warn("Direct Zeebe complete failed (likely simulated task ID): {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void triggerReviewDecision(Long applicationId, String departmentCode, String decision, String remarks) {
        log.info("Triggering review decision for App ID: {}, Dept: {}, Decision: {}", applicationId, departmentCode, decision);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        DepartmentCode code = DepartmentCode.valueOf(departmentCode);
        DepartmentReview review = departmentReviewRepository.findByApplicationIdAndDepartmentCode(applicationId, code)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for department: " + departmentCode));

        ReviewStatus status = ReviewStatus.valueOf(decision);
        review.setStatus(status);
        review.setRemarks(remarks);
        review.setReviewedAt(LocalDateTime.now());
        departmentReviewRepository.save(review);

        workflowHistoryRepository.save(new WorkflowHistory(
                app, "DEPARTMENT_REVIEW", "DECISION_" + status.name() + "_" + departmentCode,
                review.getOfficer() != null ? review.getOfficer().getUsername() : "DEPARTMENT_OFFICER",
                remarks, app.getProcessInstanceKey()
        ));

        if (status == ReviewStatus.REVISION_REQUESTED) {
            // Embedded BlueprintRevisionProcess
            int existingCycles = revisionRequestRepository.findByApplicationId(applicationId).size();
            if (existingCycles >= 3) {
                log.warn("Application ID {} exceeded maximum allowable revision cycles (3)", applicationId);
                app.setStatus(ApplicationStatus.REJECTED);
                app.setRejectedDate(LocalDateTime.now());
                app.setRejectionReason("Maximum allowable revision cycles (3) exceeded for " + departmentCode + " department");
                applicationRepository.save(app);

                workflowHistoryRepository.save(new WorkflowHistory(
                        app, "REVISION_LIMIT_EXCEEDED", "MAX_REVISIONS_REACHED", "WORKFLOW_ENGINE",
                        "Statutory maximum of 3 revision cycles exceeded. Application automatically rejected.",
                        app.getProcessInstanceKey()
                ));

                notificationService.createNotification(
                        app.getApplicant().getEmail(),
                        "Application Rejected - Maximum Revisions Exceeded",
                        "Your application has exceeded the maximum allowable 3 revision cycles. Please submit a new application.",
                        NotificationType.REJECTED
                );
                return;
            }

            int nextCycle = existingCycles + 1;
            RevisionRequest req = new RevisionRequest(app, code, remarks, nextCycle);
            revisionRequestRepository.save(req);

            app.setStatus(ApplicationStatus.REVISION_REQUESTED);
            applicationRepository.save(app);

            notificationService.createNotification(
                    app.getApplicant().getEmail(),
                    "Revision Requested by " + departmentCode + " Department",
                    "Officer remarks: " + remarks + ". Please submit updated architectural documents.",
                    NotificationType.REVISION_REQUESTED
            );
            return;
        }

        if (status == ReviewStatus.REJECTED) {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setRejectedDate(LocalDateTime.now());
            app.setRejectionReason("Rejected by " + departmentCode + " Department: " + remarks);
            applicationRepository.save(app);

            notificationService.createNotification(
                    app.getApplicant().getEmail(),
                    "Application Rejected",
                    "Your application was formally rejected by " + departmentCode + " Department. Reason: " + remarks,
                    NotificationType.REJECTED
            );
            return;
        }

        // Check if all 4 departments are APPROVED (FEEL Gateway convergence)
        List<DepartmentReview> allReviews = departmentReviewRepository.findByApplicationId(applicationId);
        boolean allApproved = allReviews.size() == 4 && allReviews.stream().allMatch(r -> r.getStatus() == ReviewStatus.APPROVED);

        if (allApproved) {
            log.info("Convergence Gateway Satisfied: All 4 departments approved application ID {}", applicationId);
            // Step 7 & 8: Municipal Fee Calculation & Create Payment Request
            jobWorkers.handleCalculateMunicipalFee(
                    app.getId(),
                    app.getBuildingType().name(),
                    app.getBuiltUpArea().doubleValue(),
                    app.getNumberOfFloors()
            );

            jobWorkers.handleCreatePaymentRequest(
                    app.getId(),
                    app.getCalculatedFee().doubleValue()
            );

            notificationService.createNotification(
                    app.getApplicant().getEmail(),
                    "Municipal Statutory Fees Generated",
                    "All departmental approvals granted! Statutory fee of INR " + app.getCalculatedFee() + " is ready for online settlement.",
                    NotificationType.FEE_GENERATED
            );
        }
    }

    @Override
    @Transactional
    public void triggerRevisionSubmitted(Long applicationId) {
        log.info("Applicant submitted revision for application ID: {}", applicationId);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        List<RevisionRequest> requests = revisionRequestRepository.findByApplicationId(applicationId);
        for (RevisionRequest req : requests) {
            if (req.getStatus() == RevisionStatus.REQUESTED) {
                req.setStatus(RevisionStatus.SUBMITTED);
                req.setRespondedAt(LocalDateTime.now());
                revisionRequestRepository.save(req);
            }

            // Re-open that department review
            departmentReviewRepository.findByApplicationIdAndDepartmentCode(applicationId, req.getDepartmentCode())
                    .ifPresent(rev -> {
                        if (rev.getStatus() == ReviewStatus.REVISION_REQUESTED || rev.getStatus() == ReviewStatus.PENDING) {
                            rev.setStatus(ReviewStatus.IN_PROGRESS);
                            departmentReviewRepository.save(rev);
                        }
                    });
        }

        app.setStatus(ApplicationStatus.UNDER_DEPARTMENT_REVIEW);
        applicationRepository.save(app);

        workflowHistoryRepository.save(new WorkflowHistory(
                app, "REVISION", "REVISED_DOCUMENTS_SUBMITTED",
                app.getApplicant().getUsername(),
                "Applicant submitted corrected blueprints and documentation",
                app.getProcessInstanceKey()
        ));
    }

    @Override
    @Transactional
    public void triggerPaymentConfirmed(Long applicationId) {
        log.info("Payment confirmed for application ID: {}", applicationId);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        // Step 11: Generate Permit (OpenPDF)
        Permit permit = jobWorkers.handleGeneratePermit(applicationId) != null ? app.getPermit() : null;

        // Step 12: Notification
        jobWorkers.handleSendNotification(
                applicationId,
                NotificationType.PERMIT_APPROVED.name(),
                app.getApplicant().getEmail(),
                permit != null ? permit.getPermitNumber() : "CB-PERMIT-2026-ACTIVE"
        );

        workflowHistoryRepository.save(new WorkflowHistory(
                app, "PERMIT_ISSUED", "PERMIT_GENERATED_AND_APPROVED", "SYSTEM",
                "Official building permit issued with 3-year statutory validity",
                app.getProcessInstanceKey()
        ));
    }
}
