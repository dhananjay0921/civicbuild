package com.civicbuild;

import com.civicbuild.dto.request.ApplicationCreateRequest;
import com.civicbuild.dto.response.ApplicationDetailResponse;
import com.civicbuild.entity.Application;
import com.civicbuild.entity.Document;
import com.civicbuild.entity.User;
import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.enums.BuildingType;
import com.civicbuild.enums.DocumentType;
import com.civicbuild.enums.VerificationStatus;
import com.civicbuild.exception.BadRequestException;
import com.civicbuild.repository.ApplicationRepository;
import com.civicbuild.repository.DocumentRepository;
import com.civicbuild.repository.UserRepository;
import com.civicbuild.service.ApplicationService;
import com.civicbuild.worker.CivicBuildJobWorkers;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class ApplicationWorkflowTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CivicBuildJobWorkers jobWorkers;

    private User applicant;

    @BeforeEach
    void setUp() {
        applicant = userRepository.findByUsername("applicant1")
                .orElseThrow(() -> new IllegalStateException("Seed applicant1 not found"));
    }

    private ApplicationCreateRequest createSampleRequest() {
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        req.setAddress("452 Lotus Boulevard, Ward 12");
        req.setSurveyNumber("SY-8899-22");
        req.setWardNumber("Ward-12");
        req.setZone("South Zone");
        req.setPlotArea(new BigDecimal("2400.00"));
        req.setOwnershipType("Freehold");
        req.setBuildingType(BuildingType.RESIDENTIAL);
        req.setConstructionPurpose("Independent Villa G+1 Construction");
        req.setNumberOfFloors(2);
        req.setBuiltUpArea(new BigDecimal("1850.00"));
        req.setBuildingHeight(new BigDecimal("7.50"));
        req.setEstimatedCost(new BigDecimal("4500000.00"));
        req.setParkingAvailable(true);
        return req;
    }

    @Test
    @DisplayName("Scenario 1: Successful Application Submission")
    void testScenario1_SuccessfulApplicationSubmission() {
        // 1. Create Draft
        ApplicationCreateRequest request = createSampleRequest();
        ApplicationDetailResponse draft = applicationService.createDraft(request, applicant);
        assertNotNull(draft.getId());
        assertEquals(ApplicationStatus.DRAFT, draft.getStatus());
        assertNotNull(draft.getApplicationNumber());

        // 2. Upload Mandatory Documents
        MockMultipartFile blueprintFile = new MockMultipartFile(
                "file", "blueprint.pdf", "application/pdf", "Sample Blueprint Content".getBytes()
        );
        Document doc1 = applicationService.uploadDocument(draft.getId(), blueprintFile, DocumentType.BUILDING_BLUEPRINT, applicant);
        assertNotNull(doc1.getId());
        assertEquals(VerificationStatus.PENDING, doc1.getVerificationStatus());

        MockMultipartFile ownershipFile = new MockMultipartFile(
                "file", "title_deed.pdf", "application/pdf", "Sample Ownership Proof".getBytes()
        );
        Document doc2 = applicationService.uploadDocument(draft.getId(), ownershipFile, DocumentType.PROPERTY_OWNERSHIP, applicant);
        assertNotNull(doc2.getId());

        // 3. Submit Application
        ApplicationDetailResponse submitted = applicationService.submitApplication(draft.getId(), applicant);
        assertNotNull(submitted.getSubmissionDate());
        assertNotNull(submitted.getProcessInstanceKey());
        // In the synchronized workflow orchestrator, submission triggers validation, land registry, document verification,
        // and transitions to UNDER_DEPARTMENT_REVIEW
        assertTrue(submitted.getStatus() == ApplicationStatus.SUBMITTED ||
                   submitted.getStatus() == ApplicationStatus.UNDER_DEPARTMENT_REVIEW);
    }

    @Test
    @DisplayName("Scenario 2: Missing Mandatory Documents Validation")
    void testScenario2_MissingMandatoryDocumentsValidation() {
        // 1. Create Draft without uploading any documents
        ApplicationCreateRequest request = createSampleRequest();
        ApplicationDetailResponse draft = applicationService.createDraft(request, applicant);

        // 2. Submitting without documents must fail with BadRequestException
        assertThrows(BadRequestException.class, () -> {
            applicationService.submitApplication(draft.getId(), applicant);
        }, "Submitting an application without mandatory documents must throw BadRequestException");
    }

    @Test
    @DisplayName("Scenario 3: Successful Document Verification")
    void testScenario3_SuccessfulDocumentVerification() {
        ApplicationCreateRequest request = createSampleRequest();
        ApplicationDetailResponse draft = applicationService.createDraft(request, applicant);

        MockMultipartFile file = new MockMultipartFile("file", "plan.pdf", "application/pdf", "Content".getBytes());
        applicationService.uploadDocument(draft.getId(), file, DocumentType.BUILDING_BLUEPRINT, applicant);

        // Execute Document Verification Worker
        Map<String, Object> result = jobWorkers.handleVerifyDocument(draft.getId(), DocumentType.BUILDING_BLUEPRINT.name());
        assertEquals("VERIFIED", result.get("verificationResult"));

        Document updatedDoc = documentRepository.findByApplicationIdAndDocumentType(draft.getId(), DocumentType.BUILDING_BLUEPRINT).orElseThrow();
        assertEquals(VerificationStatus.VERIFIED, updatedDoc.getVerificationStatus());
    }

    @Test
    @DisplayName("Scenario 4: Document Verification Rejection / Validation")
    void testScenario4_DocumentVerificationRejection() {
        ApplicationCreateRequest request = createSampleRequest();
        ApplicationDetailResponse draft = applicationService.createDraft(request, applicant);

        // Upload non-mandatory document
        MockMultipartFile file = new MockMultipartFile("file", "other.pdf", "application/pdf", "Content".getBytes());
        applicationService.uploadDocument(draft.getId(), file, DocumentType.OTHER, applicant);

        // Checking an unuploaded optional document returns VERIFIED for checklist progression without throwing BPMN error
        Map<String, Object> result = jobWorkers.handleVerifyDocument(draft.getId(), DocumentType.OTHER.name());
        assertNotNull(result);
        assertEquals("VERIFIED", result.get("verificationResult"));
    }

    @Test
    @DisplayName("Scenario 5: Missing Critical Document Business Error (BPMN Error Boundary Event)")
    void testScenario5_MissingCriticalDocumentBusinessError() {
        ApplicationCreateRequest request = createSampleRequest();
        ApplicationDetailResponse draft = applicationService.createDraft(request, applicant);

        // Notice: Do NOT upload PROPERTY_OWNERSHIP.
        // The worker must throw ZeebeBpmnError with code 'MISSING_CRITICAL_DOCUMENT'
        ZeebeBpmnError error = assertThrows(ZeebeBpmnError.class, () -> {
            jobWorkers.handleVerifyDocument(draft.getId(), DocumentType.PROPERTY_OWNERSHIP.name());
        }, "Missing mandatory document PROPERTY_OWNERSHIP must throw ZeebeBpmnError");

        assertEquals("MISSING_CRITICAL_DOCUMENT", error.getErrorCode());
        assertTrue(error.getErrorMessage().contains("PROPERTY_OWNERSHIP"));
    }
}
