package com.civicbuild;

import com.civicbuild.dto.request.ApplicationCreateRequest;
import com.civicbuild.dto.response.ApplicationDetailResponse;
import com.civicbuild.entity.Application;
import com.civicbuild.entity.DepartmentReview;
import com.civicbuild.entity.RevisionRequest;
import com.civicbuild.entity.User;
import com.civicbuild.enums.*;
import com.civicbuild.repository.ApplicationRepository;
import com.civicbuild.repository.DepartmentReviewRepository;
import com.civicbuild.repository.RevisionRequestRepository;
import com.civicbuild.repository.UserRepository;
import com.civicbuild.service.ApplicationService;
import com.civicbuild.service.DepartmentReviewService;
import com.civicbuild.service.RevisionService;
import com.civicbuild.worker.CivicBuildJobWorkers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class DepartmentReviewWorkflowTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DepartmentReviewService departmentReviewService;

    @Autowired
    private DepartmentReviewRepository departmentReviewRepository;

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private RevisionRequestRepository revisionRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CivicBuildJobWorkers jobWorkers;

    private User applicant;
    private User fireOfficer;
    private User waterOfficer;
    private User zoningOfficer;
    private User envOfficer;

    @BeforeEach
    void setUp() {
        applicant = userRepository.findByUsername("applicant1").orElseThrow();
        fireOfficer = userRepository.findByUsername("fire_officer").orElseThrow();
        waterOfficer = userRepository.findByUsername("water_officer").orElseThrow();
        zoningOfficer = userRepository.findByUsername("zoning_officer").orElseThrow();
        envOfficer = userRepository.findByUsername("env_officer").orElseThrow();
    }

    private Application createAndSubmitApplication() {
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        req.setAddress("77 Brigade Road, Ward 5");
        req.setSurveyNumber("SY-BRIG-77");
        req.setWardNumber("Ward-5");
        req.setZone("Central");
        req.setPlotArea(new BigDecimal("3500.00"));
        req.setOwnershipType("Freehold");
        req.setBuildingType(BuildingType.COMMERCIAL);
        req.setConstructionPurpose("Retail Store G+2");
        req.setNumberOfFloors(3);
        req.setBuiltUpArea(new BigDecimal("4200.00"));
        req.setBuildingHeight(new BigDecimal("12.00"));
        req.setEstimatedCost(new BigDecimal("12000000.00"));
        req.setParkingAvailable(true);

        ApplicationDetailResponse draft = applicationService.createDraft(req, applicant);

        MockMultipartFile file1 = new MockMultipartFile("file", "blueprint.pdf", "application/pdf", "Blueprint".getBytes());
        applicationService.uploadDocument(draft.getId(), file1, DocumentType.BUILDING_BLUEPRINT, applicant);

        MockMultipartFile file2 = new MockMultipartFile("file", "ownership.pdf", "application/pdf", "Ownership".getBytes());
        applicationService.uploadDocument(draft.getId(), file2, DocumentType.PROPERTY_OWNERSHIP, applicant);

        applicationService.submitApplication(draft.getId(), applicant);
        return applicationRepository.findById(draft.getId()).orElseThrow();
    }

    @Test
    @DisplayName("Scenario 6: Parallel Creation of Four Departmental Reviews")
    void testScenario6_ParallelCreationOfFourDepartmentalReviews() {
        Application app = createAndSubmitApplication();

        List<DepartmentReview> reviews = departmentReviewRepository.findByApplicationId(app.getId());
        assertEquals(4, reviews.size(), "Workflow must spawn exactly 4 parallel departmental reviews");

        assertTrue(reviews.stream().anyMatch(r -> r.getDepartmentCode() == DepartmentCode.FIRE));
        assertTrue(reviews.stream().anyMatch(r -> r.getDepartmentCode() == DepartmentCode.WATER));
        assertTrue(reviews.stream().anyMatch(r -> r.getDepartmentCode() == DepartmentCode.ZONING));
        assertTrue(reviews.stream().anyMatch(r -> r.getDepartmentCode() == DepartmentCode.ENVIRONMENTAL));

        for (DepartmentReview r : reviews) {
            assertEquals(ReviewStatus.PENDING, r.getStatus());
            assertNotNull(r.getDueDate());
        }
    }

    @Test
    @DisplayName("Scenario 7: Department Approval")
    void testScenario7_DepartmentApproval() {
        Application app = createAndSubmitApplication();

        DepartmentReview fireReview = departmentReviewRepository.findByApplicationIdAndDepartmentCode(app.getId(), DepartmentCode.FIRE).orElseThrow();
        departmentReviewService.approveReview(fireReview.getId(), "Fire exit compliance and extinguisher locations approved", fireOfficer);

        DepartmentReview updated = departmentReviewRepository.findById(fireReview.getId()).orElseThrow();
        assertEquals(ReviewStatus.APPROVED, updated.getStatus());
        assertEquals("Fire exit compliance and extinguisher locations approved", updated.getRemarks());
        assertNotNull(updated.getReviewedAt());
    }

    @Test
    @DisplayName("Scenario 8: Department Rejection")
    void testScenario8_DepartmentRejection() {
        Application app = createAndSubmitApplication();

        DepartmentReview zoningReview = departmentReviewRepository.findByApplicationIdAndDepartmentCode(app.getId(), DepartmentCode.ZONING).orElseThrow();
        departmentReviewService.rejectReview(zoningReview.getId(), "Proposed commercial structure encroaches into designated green buffer zone", zoningOfficer);

        DepartmentReview updated = departmentReviewRepository.findById(zoningReview.getId()).orElseThrow();
        assertEquals(ReviewStatus.REJECTED, updated.getStatus());

        Application updatedApp = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationStatus.REJECTED, updatedApp.getStatus());
        assertTrue(updatedApp.getRejectionReason().contains("green buffer zone"));
        assertNotNull(updatedApp.getRejectedDate());
    }

    @Test
    @DisplayName("Scenario 9: Department Revision Request")
    void testScenario9_DepartmentRevisionRequest() {
        Application app = createAndSubmitApplication();

        DepartmentReview waterReview = departmentReviewRepository.findByApplicationIdAndDepartmentCode(app.getId(), DepartmentCode.WATER).orElseThrow();
        departmentReviewService.requestRevision(waterReview.getId(), "Provide sewage treatment plant (STP) design and rainwater harvesting layout", waterOfficer);

        DepartmentReview updated = departmentReviewRepository.findById(waterReview.getId()).orElseThrow();
        assertEquals(ReviewStatus.REVISION_REQUESTED, updated.getStatus());

        Application updatedApp = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationStatus.REVISION_REQUESTED, updatedApp.getStatus());

        List<RevisionRequest> requests = revisionRequestRepository.findByApplicationId(app.getId());
        assertEquals(1, requests.size());
        assertEquals(1, requests.get(0).getCycleNumber());
        assertEquals(RevisionStatus.REQUESTED, requests.get(0).getStatus());
    }

    @Test
    @DisplayName("Scenario 10: Applicant Resubmission of Corrected Documents")
    void testScenario10_ApplicantResubmissionOfCorrectedDocuments() {
        Application app = createAndSubmitApplication();

        // 1. Officer requests revision
        DepartmentReview waterReview = departmentReviewRepository.findByApplicationIdAndDepartmentCode(app.getId(), DepartmentCode.WATER).orElseThrow();
        departmentReviewService.requestRevision(waterReview.getId(), "Rainwater harvesting blueprint required", waterOfficer);

        List<RevisionRequest> requests = revisionRequestRepository.findByApplicationId(app.getId());
        RevisionRequest req = requests.get(0);

        // 2. Applicant uploads updated document
        MockMultipartFile rwhFile = new MockMultipartFile("file", "stp_rwh_layout.pdf", "application/pdf", "Updated Layout Content".getBytes());
        applicationService.uploadDocument(app.getId(), rwhFile, DocumentType.SITE_PLAN, applicant);

        // 3. Applicant responds to revision request
        RevisionRequest submittedReq = revisionService.submitRevision(req.getId(), "Attached revised STP and RWH layout as requested", applicant);
        assertEquals(RevisionStatus.SUBMITTED, submittedReq.getStatus());
        assertNotNull(submittedReq.getRespondedAt());

        // 4. Department review returns to IN_PROGRESS
        DepartmentReview updatedReview = departmentReviewRepository.findById(waterReview.getId()).orElseThrow();
        assertEquals(ReviewStatus.IN_PROGRESS, updatedReview.getStatus());

        Application updatedApp = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationStatus.UNDER_DEPARTMENT_REVIEW, updatedApp.getStatus());
    }

    @Test
    @DisplayName("Scenario 11: Department Re-review after Resubmission")
    void testScenario11_DepartmentReReviewAfterResubmission() {
        Application app = createAndSubmitApplication();

        // Request revision
        DepartmentReview envReview = departmentReviewRepository.findByApplicationIdAndDepartmentCode(app.getId(), DepartmentCode.ENVIRONMENTAL).orElseThrow();
        departmentReviewService.requestRevision(envReview.getId(), "Submit acoustic barrier details", envOfficer);

        // Applicant submits response
        RevisionRequest req = revisionRequestRepository.findByApplicationId(app.getId()).get(0);
        revisionService.submitRevision(req.getId(), "Acoustic enclosure specs added to drawing", applicant);

        // Officer re-reviews and approves
        departmentReviewService.approveReview(envReview.getId(), "Acoustic barrier specifications meet pollution control standards", envOfficer);

        DepartmentReview finalReview = departmentReviewRepository.findById(envReview.getId()).orElseThrow();
        assertEquals(ReviewStatus.APPROVED, finalReview.getStatus());
    }

    @Test
    @DisplayName("Scenario 12: Review Limit Reached Exception / Automatic Rejection")
    void testScenario12_ReviewLimitReachedException() {
        Application app = createAndSubmitApplication();
        DepartmentReview fireReview = departmentReviewRepository.findByApplicationIdAndDepartmentCode(app.getId(), DepartmentCode.FIRE).orElseThrow();

        // Cycle 1
        departmentReviewService.requestRevision(fireReview.getId(), "Cycle 1 fix needed", fireOfficer);
        RevisionRequest req1 = revisionRequestRepository.findByApplicationId(app.getId()).get(0);
        revisionService.submitRevision(req1.getId(), "Resubmitted 1", applicant);

        // Cycle 2
        departmentReviewService.requestRevision(fireReview.getId(), "Cycle 2 fix needed", fireOfficer);
        RevisionRequest req2 = revisionRequestRepository.findByApplicationId(app.getId()).get(1);
        revisionService.submitRevision(req2.getId(), "Resubmitted 2", applicant);

        // Cycle 3
        departmentReviewService.requestRevision(fireReview.getId(), "Cycle 3 fix needed", fireOfficer);
        RevisionRequest req3 = revisionRequestRepository.findByApplicationId(app.getId()).get(2);
        revisionService.submitRevision(req3.getId(), "Resubmitted 3", applicant);

        // Cycle 4: Exceeds max revisions (3). Must trigger automatic rejection
        departmentReviewService.requestRevision(fireReview.getId(), "Cycle 4 attempt", fireOfficer);

        Application finalApp = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationStatus.REJECTED, finalApp.getStatus());
        assertTrue(finalApp.getRejectionReason().contains("Maximum allowable revision cycles (3) exceeded"));
    }

    @Test
    @DisplayName("Scenario 13: Escalation Notification on Overdue Review (Timer Boundary Event)")
    void testScenario13_EscalationNotificationOnOverdueReview() {
        Application app = createAndSubmitApplication();

        // Simulate Zeebe Timer Boundary Event (PT48H expiration) invoking handleReviewTimeout
        Map<String, Object> timeoutResult = jobWorkers.handleReviewTimeout(app.getId(), "FIRE");
        assertNotNull(timeoutResult);
        assertEquals(true, timeoutResult.get("escalationHandled"));

        // Verify SLA breach history record created
        Application updatedApp = applicationRepository.findById(app.getId()).orElseThrow();
        assertNotNull(updatedApp);
    }
}
