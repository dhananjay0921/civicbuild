package com.civicbuild;

import com.civicbuild.dto.request.ApplicationCreateRequest;
import com.civicbuild.dto.response.ApplicationDetailResponse;
import com.civicbuild.entity.*;
import com.civicbuild.enums.*;
import com.civicbuild.repository.*;
import com.civicbuild.service.*;
import com.civicbuild.worker.CivicBuildJobWorkers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class PermitGenerationAndE2ETest {

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
    private PaymentService paymentService;

    @Autowired
    private PermitService permitService;

    @Autowired
    private PermitRepository permitRepository;

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

    @Test
    @DisplayName("Scenario 17: Permit Generation and PDF Creation")
    void testScenario17_PermitGenerationAndPdfCreation() throws Exception {
        // Create an approved application
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        req.setAddress("12 Palm Meadows, Ward 8");
        req.setSurveyNumber("SY-PERMIT-12");
        req.setWardNumber("Ward-8");
        req.setZone("East Zone");
        req.setPlotArea(new BigDecimal("3000.00"));
        req.setOwnershipType("Freehold");
        req.setBuildingType(BuildingType.RESIDENTIAL);
        req.setConstructionPurpose("Two-Story Luxury Residence");
        req.setNumberOfFloors(2);
        req.setBuiltUpArea(new BigDecimal("2600.00"));
        req.setBuildingHeight(new BigDecimal("8.20"));
        req.setEstimatedCost(new BigDecimal("7500000.00"));
        req.setParkingAvailable(true);

        ApplicationDetailResponse draft = applicationService.createDraft(req, applicant);
        Application app = applicationRepository.findById(draft.getId()).orElseThrow();
        app.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(app);

        // Generate Permit Record and PDF
        Permit permit = permitService.createPermitRecord(app.getId());
        assertNotNull(permit);
        assertNotNull(permit.getPermitNumber());
        assertTrue(permit.getPermitNumber().startsWith("CB-PERMIT-"));
        assertNotNull(permit.getQrCodeData());
        assertNotNull(permit.getExpiryDate());
        assertNotNull(permit.getPdfPath());

        // Verify PDF on disk
        File pdfFile = new File(permit.getPdfPath());
        assertTrue(pdfFile.exists(), "Generated PDF permit must physically exist on the filesystem");
        assertTrue(pdfFile.length() > 500, "PDF file size must be substantial (>500 bytes)");

        // Verify PDF magic header bytes: %PDF-
        try (FileInputStream fis = new FileInputStream(pdfFile)) {
            byte[] header = new byte[5];
            int read = fis.read(header);
            assertEquals(5, read);
            assertEquals("%PDF-", new String(header, StandardCharsets.US_ASCII));
        }
    }

    @Test
    @DisplayName("Scenario 18: End-to-End Permit Approval Simulation")
    void testScenario18_EndToEndPermitApprovalSimulation() {
        // Step 1: Applicant creates Draft Application
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        req.setAddress("500 Tech Park Way, Whitefield");
        req.setSurveyNumber("SY-E2E-2026");
        req.setWardNumber("Ward-84");
        req.setZone("Mahadevapura");
        req.setPlotArea(new BigDecimal("5000.00"));
        req.setOwnershipType("Commercial Leasehold");
        req.setBuildingType(BuildingType.COMMERCIAL);
        req.setConstructionPurpose("Software Technology Park Facility");
        req.setNumberOfFloors(4);
        req.setBuiltUpArea(new BigDecimal("8000.00"));
        req.setBuildingHeight(new BigDecimal("16.00"));
        req.setEstimatedCost(new BigDecimal("35000000.00"));
        req.setParkingAvailable(true);

        ApplicationDetailResponse draft = applicationService.createDraft(req, applicant);
        assertNotNull(draft.getId());
        assertEquals(ApplicationStatus.DRAFT, draft.getStatus());

        // Step 2: Upload Statutory Documents
        MockMultipartFile blueprint = new MockMultipartFile("file", "blueprint_arch.pdf", "application/pdf", "CAD Blueprint".getBytes());
        applicationService.uploadDocument(draft.getId(), blueprint, DocumentType.BUILDING_BLUEPRINT, applicant);

        MockMultipartFile ownership = new MockMultipartFile("file", "lease_agreement.pdf", "application/pdf", "Lease Contract".getBytes());
        applicationService.uploadDocument(draft.getId(), ownership, DocumentType.PROPERTY_OWNERSHIP, applicant);

        MockMultipartFile structural = new MockMultipartFile("file", "structural_cert.pdf", "application/pdf", "Stability Cert".getBytes());
        applicationService.uploadDocument(draft.getId(), structural, DocumentType.STRUCTURAL_STABILITY, applicant);

        // Step 3: Formal Submission (Triggers Camunda Workflow, validation, land title search, sequential doc checklist, parallel review instances)
        ApplicationDetailResponse submitted = applicationService.submitApplication(draft.getId(), applicant);
        assertEquals(ApplicationStatus.UNDER_DEPARTMENT_REVIEW, submitted.getStatus());

        List<DepartmentReview> reviews = departmentReviewRepository.findByApplicationId(draft.getId());
        assertEquals(4, reviews.size());

        // Step 4: Fire Department Review -> APPROVAL
        DepartmentReview fire = departmentReviewRepository.findByApplicationIdAndDepartmentCode(draft.getId(), DepartmentCode.FIRE).orElseThrow();
        departmentReviewService.approveReview(fire.getId(), "Fire suppression and hydrant design verified", fireOfficer);

        // Step 5: Water Department Review -> REVISION REQUESTED
        DepartmentReview water = departmentReviewRepository.findByApplicationIdAndDepartmentCode(draft.getId(), DepartmentCode.WATER).orElseThrow();
        departmentReviewService.requestRevision(water.getId(), "Include dual plumbing greywater recycle schematics", waterOfficer);

        Application appUnderRevision = applicationRepository.findById(draft.getId()).orElseThrow();
        assertEquals(ApplicationStatus.REVISION_REQUESTED, appUnderRevision.getStatus());

        // Step 6: Applicant resubmits revision
        RevisionRequest revReq = revisionRequestRepository.findByApplicationId(draft.getId()).get(0);
        MockMultipartFile plumbingDoc = new MockMultipartFile("file", "dual_plumbing_dwg.pdf", "application/pdf", "Plumbing Drawing".getBytes());
        applicationService.uploadDocument(draft.getId(), plumbingDoc, DocumentType.OTHER, applicant);
        revisionService.submitRevision(revReq.getId(), "Uploaded dual plumbing schematics", applicant);

        // Step 7: Water Officer Re-reviews -> APPROVAL
        departmentReviewService.approveReview(water.getId(), "Dual plumbing and greywater schematics approved", waterOfficer);

        // Step 8: Zoning Department Review -> APPROVAL
        DepartmentReview zoning = departmentReviewRepository.findByApplicationIdAndDepartmentCode(draft.getId(), DepartmentCode.ZONING).orElseThrow();
        departmentReviewService.approveReview(zoning.getId(), "FAR and set-back clearances verified under master plan", zoningOfficer);

        // Step 9: Environmental Department Review -> APPROVAL
        DepartmentReview env = departmentReviewRepository.findByApplicationIdAndDepartmentCode(draft.getId(), DepartmentCode.ENVIRONMENTAL).orElseThrow();
        departmentReviewService.approveReview(env.getId(), "EIA report and waste management plant cleared", envOfficer);

        // Step 10: All 4 approved -> Convergence Gateway satisfied -> Fee Calculation & Payment Request auto-generated
        Application appFeePending = applicationRepository.findById(draft.getId()).orElseThrow();
        assertEquals(ApplicationStatus.PAYMENT_PENDING, appFeePending.getStatus());
        assertNotNull(appFeePending.getCalculatedFee());
        assertTrue(appFeePending.getCalculatedFee().compareTo(BigDecimal.ZERO) > 0);

        // Step 11: Applicant settles fee
        Payment payment = paymentService.getPaymentForApplication(draft.getId(), applicant);
        assertNotNull(payment);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());

        Payment confirmed = paymentService.confirmPayment(draft.getId(), payment.getTransactionRef(), true, applicant);
        assertEquals(PaymentStatus.SUCCESS, confirmed.getStatus());

        // Step 12: Final State Verification
        Application finalApp = applicationRepository.findById(draft.getId()).orElseThrow();
        assertEquals(ApplicationStatus.APPROVED, finalApp.getStatus());
        assertNotNull(finalApp.getApprovedDate());

        Permit finalPermit = permitRepository.findByApplicationId(draft.getId()).orElseThrow();
        assertNotNull(finalPermit.getPermitNumber());
        assertNotNull(finalPermit.getPdfPath());
        assertTrue(new File(finalPermit.getPdfPath()).exists());
    }
}
