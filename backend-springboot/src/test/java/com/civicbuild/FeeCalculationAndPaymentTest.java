package com.civicbuild;

import com.civicbuild.dto.request.ApplicationCreateRequest;
import com.civicbuild.dto.response.ApplicationDetailResponse;
import com.civicbuild.entity.Application;
import com.civicbuild.entity.Payment;
import com.civicbuild.entity.User;
import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.enums.BuildingType;
import com.civicbuild.enums.PaymentMethod;
import com.civicbuild.enums.PaymentStatus;
import com.civicbuild.exception.BadRequestException;
import com.civicbuild.repository.ApplicationRepository;
import com.civicbuild.repository.PaymentRepository;
import com.civicbuild.repository.UserRepository;
import com.civicbuild.service.ApplicationService;
import com.civicbuild.service.PaymentService;
import com.civicbuild.worker.CivicBuildJobWorkers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class FeeCalculationAndPaymentTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CivicBuildJobWorkers jobWorkers;

    private User applicant;

    @BeforeEach
    void setUp() {
        applicant = userRepository.findByUsername("applicant1").orElseThrow();
    }

    private Application createDraft(BuildingType buildingType, BigDecimal area, int floors) {
        ApplicationCreateRequest req = new ApplicationCreateRequest();
        req.setAddress("100 MG Road, Zone Central");
        req.setSurveyNumber("SY-FEE-TEST");
        req.setWardNumber("Ward-1");
        req.setZone("Central");
        req.setPlotArea(area.multiply(new BigDecimal("1.5")));
        req.setOwnershipType("Freehold");
        req.setBuildingType(buildingType);
        req.setConstructionPurpose("Statutory Fee Verification");
        req.setNumberOfFloors(floors);
        req.setBuiltUpArea(area);
        req.setBuildingHeight(new BigDecimal("9.00"));
        req.setEstimatedCost(new BigDecimal("5000000.00"));
        req.setParkingAvailable(true);

        ApplicationDetailResponse draft = applicationService.createDraft(req, applicant);
        return applicationRepository.findById(draft.getId()).orElseThrow();
    }

    @Test
    @DisplayName("Scenario 14: Fee Calculation Accuracy Across All Four Building Types")
    void testScenario14_FeeCalculationAccuracyAcrossFourBuildingTypes() {
        // 1. Residential: Base 5000 + (2000 sq.ft * 2.5) + (2 floors * 1500) = 5000 + 5000 + 3000 = 13,000.0 INR
        Application resApp = createDraft(BuildingType.RESIDENTIAL, new BigDecimal("2000.00"), 2);
        Map<String, Object> resResult = jobWorkers.handleCalculateMunicipalFee(resApp.getId(), "RESIDENTIAL", 2000.0, 2);
        assertEquals(13000.0, ((Number) resResult.get("totalFee")).doubleValue(), 0.01);

        // 2. Commercial: Base 15000 + (3000 sq.ft * 6.0) + (3 floors * 3500) = 15000 + 18000 + 10500 = 43,500.0 INR
        Application commApp = createDraft(BuildingType.COMMERCIAL, new BigDecimal("3000.00"), 3);
        Map<String, Object> commResult = jobWorkers.handleCalculateMunicipalFee(commApp.getId(), "COMMERCIAL", 3000.0, 3);
        assertEquals(43500.0, ((Number) commResult.get("totalFee")).doubleValue(), 0.01);

        // 3. Industrial: Base 25000 + (4000 sq.ft * 8.0) + (1 floor * 5000) = 25000 + 32000 + 5000 = 62,000.0 INR
        Application indApp = createDraft(BuildingType.INDUSTRIAL, new BigDecimal("4000.00"), 1);
        Map<String, Object> indResult = jobWorkers.handleCalculateMunicipalFee(indApp.getId(), "INDUSTRIAL", 4000.0, 1);
        assertEquals(62000.0, ((Number) indResult.get("totalFee")).doubleValue(), 0.01);

        // 4. Institutional: Base 10000 + (2500 sq.ft * 4.0) + (4 floors * 2000) = 10000 + 10000 + 8000 = 28,000.0 INR
        Application instApp = createDraft(BuildingType.INSTITUTIONAL, new BigDecimal("2500.00"), 4);
        Map<String, Object> instResult = jobWorkers.handleCalculateMunicipalFee(instApp.getId(), "INSTITUTIONAL", 2500.0, 4);
        assertEquals(28000.0, ((Number) instResult.get("totalFee")).doubleValue(), 0.01);
    }

    @Test
    @DisplayName("Scenario 15: Payment Success Flow")
    void testScenario15_PaymentSuccessFlow() {
        Application app = createDraft(BuildingType.RESIDENTIAL, new BigDecimal("1000.00"), 1);
        jobWorkers.handleCalculateMunicipalFee(app.getId(), "RESIDENTIAL", 1000.0, 1);

        // Initiate Payment
        Payment payment = paymentService.initiatePayment(app.getId(), PaymentMethod.UPI, applicant);
        assertNotNull(payment);
        assertNotNull(payment.getTransactionRef());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());

        // Confirm Payment (mock success = true)
        Payment confirmed = paymentService.confirmPayment(app.getId(), payment.getTransactionRef(), true, applicant);
        assertEquals(PaymentStatus.SUCCESS, confirmed.getStatus());
        assertNotNull(confirmed.getPaymentDate());

        // Verify application status updated to APPROVED after successful payment and permit issuance
        Application updated = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationStatus.APPROVED, updated.getStatus());
    }

    @Test
    @DisplayName("Scenario 16: Payment Retry on Failure")
    void testScenario16_PaymentRetryOnFailure() {
        Application app = createDraft(BuildingType.COMMERCIAL, new BigDecimal("1500.00"), 2);
        jobWorkers.handleCalculateMunicipalFee(app.getId(), "COMMERCIAL", 1500.0, 2);

        // 1. Initial attempt fails
        Payment failedPayment = paymentService.initiatePayment(app.getId(), PaymentMethod.NET_BANKING, applicant);
        assertThrows(BadRequestException.class, () -> {
            paymentService.confirmPayment(app.getId(), failedPayment.getTransactionRef(), false, applicant);
        });

        Payment failedConfirmed = paymentRepository.findByTransactionRef(failedPayment.getTransactionRef()).orElseThrow();
        assertEquals(PaymentStatus.FAILED, failedConfirmed.getStatus());

        // Application status remains PAYMENT_PENDING
        Application appAfterFail = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationStatus.PAYMENT_PENDING, appAfterFail.getStatus());

        // 2. Retry payment with new transaction reference
        Payment retryPayment = paymentService.initiatePayment(app.getId(), PaymentMethod.CREDIT_CARD, applicant);
        assertNotNull(retryPayment.getTransactionRef());
        assertNotEquals(failedPayment.getTransactionRef(), retryPayment.getTransactionRef());

        // 3. Retry succeeds
        Payment successPayment = paymentService.confirmPayment(app.getId(), retryPayment.getTransactionRef(), true, applicant);
        assertEquals(PaymentStatus.SUCCESS, successPayment.getStatus());

        Application appAfterSuccess = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationStatus.APPROVED, appAfterSuccess.getStatus());
    }
}
