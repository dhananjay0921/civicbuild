package com.civicbuild;

import com.civicbuild.dto.request.*;
import com.civicbuild.dto.response.ApplicationDetailResponse;
import com.civicbuild.dto.response.AuthResponse;
import com.civicbuild.entity.*;
import com.civicbuild.enums.*;
import com.civicbuild.repository.*;
import com.civicbuild.service.*;
import com.civicbuild.worker.CivicBuildJobWorkers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CivicBuildIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DepartmentReviewService departmentReviewService;

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PermitService permitService;

    @Autowired
    private CivicBuildJobWorkers jobWorkers;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DepartmentReviewRepository departmentReviewRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RevisionRequestRepository revisionRequestRepository;

    private static String applicantToken;
    private static Long createdAppId;

    @Test
    @Order(1)
    @DisplayName("Scenario 14: Authentication API (Login & Register)")
    public void testAuthentication() throws Exception {
        // Test Login with pre-seeded applicant1 account
        LoginRequest loginReq = new LoginRequest("applicant1", "password123");
        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        AuthResponse auth = objectMapper.readTree(json).get("data").traverse(objectMapper).readValueAs(AuthResponse.class);
        applicantToken = auth.getToken();
        assertNotNull(applicantToken);

        // Test GET /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("applicant1"));
    }

    @Test
    @Order(2)
    @DisplayName("Scenario 15: Role-based Authorization Security Check")
    public void testRoleBasedAuthorization() throws Exception {
        // Applicant should NOT be able to access Admin users endpoint
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    @DisplayName("Scenario 17: API Request Body Validation Failure")
    public void testApiValidation() throws Exception {
        // Attempt to create application with missing mandatory fields
        ApplicationCreateRequest invalidReq = new ApplicationCreateRequest();
        // Missing address, surveyNumber, etc.
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(4)
    @DisplayName("Scenario 1 & 16: Draft Application Creation & Database Persistence")
    public void testCreateDraftApplication() throws Exception {
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        req.setAddress("Sector 12, Cyber Park Avenue");
        req.setSurveyNumber("SRV-2026/8812");
        req.setWardNumber("Ward-12");
        req.setZone("Central Urban Zone");
        req.setPlotArea(new BigDecimal("3200.00"));
        req.setOwnershipType("FREEHOLD");
        req.setBuildingType(BuildingType.COMMERCIAL);
        req.setConstructionPurpose("Commercial Complex");
        req.setNumberOfFloors(4);
        req.setBuiltUpArea(new BigDecimal("9600.00"));
        req.setBuildingHeight(new BigDecimal("16.00"));
        req.setEstimatedCost(new BigDecimal("22000000.00"));
        req.setParkingAvailable(true);

        String json = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        ApplicationDetailResponse res = objectMapper.readTree(json).get("data").traverse(objectMapper).readValueAs(ApplicationDetailResponse.class);
        createdAppId = res.getId();
        assertNotNull(createdAppId);

        // Verify persistence in DB
        Application persisted = applicationRepository.findById(createdAppId).orElse(null);
        assertNotNull(persisted);
        assertEquals(ApplicationStatus.DRAFT, persisted.getStatus());
        assertEquals("SRV-2026/8812", persisted.getProperty().getSurveyNumber());
    }

    @Test
    @Order(5)
    @DisplayName("Scenario 2: Missing Mandatory Document Check Failure on Submit")
    public void testMissingMandatoryDocument() throws Exception {
        // Attempting to submit application before uploading mandatory documents
        mockMvc.perform(post("/api/applications/" + createdAppId + "/submit")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please upload statutory documentation (Blueprint, Ownership Proof) before submission"));
    }

    @Test
    @Order(6)
    @DisplayName("Scenario 3: Successful Document Upload & Verification")
    public void testDocumentUpload() throws Exception {
        MockMultipartFile deedFile = new MockMultipartFile(
                "file", "Title_Deed_8812.pdf", "application/pdf",
                "Mock Title Deed Content for CivicBuild".getBytes()
        );

        mockMvc.perform(multipart("/api/applications/" + createdAppId + "/documents")
                        .file(deedFile)
                        .param("documentType", "PROPERTY_OWNERSHIP")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentType").value("PROPERTY_OWNERSHIP"));

        MockMultipartFile blueprintFile = new MockMultipartFile(
                "file", "Commercial_Blueprint_v1.pdf", "application/pdf",
                "Mock Blueprint Drawings Content".getBytes()
        );

        mockMvc.perform(multipart("/api/applications/" + createdAppId + "/documents")
                        .file(blueprintFile)
                        .param("documentType", "BUILDING_BLUEPRINT")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentType").value("BUILDING_BLUEPRINT"));
    }

    @Test
    @Order(7)
    @DisplayName("Scenario 1: Successful Application Submission and Process Initialization")
    public void testSubmitApplication() throws Exception {
        mockMvc.perform(post("/api/applications/" + createdAppId + "/submit")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_DEPARTMENT_REVIEW"));

        Application app = applicationRepository.findById(createdAppId).orElse(null);
        assertNotNull(app);
        assertEquals(ApplicationStatus.UNDER_DEPARTMENT_REVIEW, app.getStatus());
        assertNotNull(app.getProcessInstanceKey());

        // Verify that 4 parallel reviews were created!
        List<DepartmentReview> reviews = departmentReviewRepository.findByApplicationId(createdAppId);
        assertEquals(4, reviews.size());
    }

    @Test
    @Order(8)
    @DisplayName("Scenario 8: Department Review Timeout Escalation Service Task")
    public void testReviewTimeoutEscalation() {
        Map<String, Object> result = jobWorkers.handleReviewTimeout(createdAppId, "ZONING");
        assertTrue((Boolean) result.get("escalationHandled"));
    }

    @Test
    @Order(9)
    @DisplayName("Scenario 6: Department Revision Request")
    public void testDepartmentRevisionRequest() {
        User fireOfficer = userRepository.findByUsername("fire_officer").orElseThrow();
        DepartmentReview fireReview = departmentReviewRepository.findByApplicationIdAndDepartmentCode(createdAppId, DepartmentCode.FIRE).orElseThrow();

        departmentReviewService.requestRevision(fireReview.getId(), "Staircase width must be increased from 1.2m to 1.5m as per NBC commercial norms", fireOfficer);

        Application app = applicationRepository.findById(createdAppId).orElseThrow();
        assertEquals(ApplicationStatus.REVISION_REQUESTED, app.getStatus());
    }

    @Test
    @Order(10)
    @DisplayName("Scenario 7: Successful Revision Resubmission")
    public void testRevisionResubmission() {
        User applicant = userRepository.findByUsername("applicant1").orElseThrow();
        var revisions = appRevisions(createdAppId);
        assertFalse(revisions.isEmpty());

        revisionService.submitRevision(revisions.get(0).getId(), "Amended blueprints attached with 1.5m staircase width", applicant);

        Application app = applicationRepository.findById(createdAppId).orElseThrow();
        assertEquals(ApplicationStatus.UNDER_DEPARTMENT_REVIEW, app.getStatus());
    }

    @Test
    @Order(11)
    @DisplayName("Scenario 4: Parallel Department Approvals & Gateways")
    public void testParallelDepartmentApprovals() {
        User fireOfficer = userRepository.findByUsername("fire_officer").orElseThrow();
        User waterOfficer = userRepository.findByUsername("water_officer").orElseThrow();
        User zoningOfficer = userRepository.findByUsername("zoning_officer").orElseThrow();
        User envOfficer = userRepository.findByUsername("env_officer").orElseThrow();

        var fireRev = departmentReviewRepository.findByApplicationIdAndDepartmentCode(createdAppId, DepartmentCode.FIRE).orElseThrow();
        var waterRev = departmentReviewRepository.findByApplicationIdAndDepartmentCode(createdAppId, DepartmentCode.WATER).orElseThrow();
        var zoningRev = departmentReviewRepository.findByApplicationIdAndDepartmentCode(createdAppId, DepartmentCode.ZONING).orElseThrow();
        var envRev = departmentReviewRepository.findByApplicationIdAndDepartmentCode(createdAppId, DepartmentCode.ENVIRONMENTAL).orElseThrow();

        departmentReviewService.approveReview(fireRev.getId(), "Fire clearance approved", fireOfficer);
        departmentReviewService.approveReview(waterRev.getId(), "Water connection approved", waterOfficer);
        departmentReviewService.approveReview(zoningRev.getId(), "Zoning FAR clearance approved", zoningOfficer);
        departmentReviewService.approveReview(envRev.getId(), "Environmental clearance approved", envOfficer);

        // Once all 4 approved, the convergence gateway triggers Fee Calculation and Payment Request!
        Application app = applicationRepository.findById(createdAppId).orElseThrow();
        assertEquals(ApplicationStatus.PAYMENT_PENDING, app.getStatus());
        assertNotNull(app.getCalculatedFee());
        assertTrue(app.getCalculatedFee().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Order(12)
    @DisplayName("Scenario 10: Failed Payment Simulation and Retry")
    public void testFailedPayment() {
        User applicant = userRepository.findByUsername("applicant1").orElseThrow();
        Payment payment = paymentService.getPaymentForApplication(createdAppId, applicant);

        // Attempt payment with mockSuccess = false
        assertThrows(RuntimeException.class, () -> {
            paymentService.confirmPayment(createdAppId, payment.getTransactionRef(), false, applicant);
        });

        Payment failedPayment = paymentRepository.findByTransactionRef(payment.getTransactionRef()).orElseThrow();
        assertEquals(PaymentStatus.FAILED, failedPayment.getStatus());
    }

    @Test
    @Order(13)
    @DisplayName("Scenario 11, 12 & 13: Successful Payment, Permit Generation & Notification Delivery")
    public void testSuccessfulPaymentAndPermitGeneration() {
        User applicant = userRepository.findByUsername("applicant1").orElseThrow();
        
        // Initiate new payment session
        Payment retryPayment = paymentService.initiatePayment(createdAppId, PaymentMethod.UPI, applicant);
        assertNotNull(retryPayment);

        // Confirm successful payment
        Payment confirmed = paymentService.confirmPayment(createdAppId, retryPayment.getTransactionRef(), true, applicant);
        assertEquals(PaymentStatus.SUCCESS, confirmed.getStatus());

        // Verify Application transitioned to APPROVED
        Application app = applicationRepository.findById(createdAppId).orElseThrow();
        assertEquals(ApplicationStatus.APPROVED, app.getStatus());
        assertNotNull(app.getApprovedDate());

        // Verify Permit Record generated
        Permit permit = permitService.getPermitByApplicationId(createdAppId);
        assertNotNull(permit);
        assertTrue(permit.getPermitNumber().startsWith("CB-PERMIT-2026-"));

        // Verify Notification delivery
        List<Notification> notifs = notificationRepository.findByRecipientEmailOrderBySentAtDesc(applicant.getEmail());
        assertFalse(notifs.isEmpty());
        assertEquals(NotificationType.PERMIT_APPROVED, notifs.get(0).getType());
    }

    @Test
    @Order(14)
    @DisplayName("Scenario 5: Department Rejection Path")
    public void testDepartmentRejection() {
        // Create a separate test application to verify rejection path
        User applicant = userRepository.findByUsername("applicant1").orElseThrow();
        Application app = new Application();
        app.setApplicationNumber("CB-2026-REJECT-TEST");
        app.setApplicant(applicant);
        app.setProperty(new Property("45 Illegal Enclave", "SRV-ILLEGAL", "Ward-99", "Prohibited Zone", new BigDecimal("1000.00"), "FREEHOLD"));
        app.setStatus(ApplicationStatus.UNDER_DEPARTMENT_REVIEW);
        app.setBuildingType(BuildingType.INDUSTRIAL);
        app.setConstructionPurpose("Chemical Processing Unit in Residential Ward");
        app.setNumberOfFloors(1);
        app.setBuiltUpArea(new BigDecimal("1000.00"));
        app.setBuildingHeight(new BigDecimal("5.00"));
        app.setEstimatedCost(new BigDecimal("1000000.00"));
        app = applicationRepository.save(app);

        DepartmentReview zoningReview = new DepartmentReview(app, DepartmentCode.ZONING, null);
        zoningReview = departmentReviewRepository.save(zoningReview);

        User zoningOfficer = userRepository.findByUsername("zoning_officer").orElseThrow();
        departmentReviewService.rejectReview(zoningReview.getId(), "Industrial chemical unit prohibited in residential master plan", zoningOfficer);

        Application rejectedApp = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationStatus.REJECTED, rejectedApp.getStatus());
        assertNotNull(rejectedApp.getRejectedDate());
        assertTrue(rejectedApp.getRejectionReason().contains("Industrial chemical unit prohibited"));
    }

    private List<RevisionRequest> appRevisions(Long appId) {
        return revisionRequestRepository.findByApplicationId(appId);
    }
}
