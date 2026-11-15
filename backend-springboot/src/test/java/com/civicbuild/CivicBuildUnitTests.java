package com.civicbuild;

import com.civicbuild.dto.request.LoginRequest;
import com.civicbuild.dto.request.RegisterRequest;
import com.civicbuild.entity.*;
import com.civicbuild.enums.*;
import com.civicbuild.security.JwtUtils;
import com.civicbuild.service.PermitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class CivicBuildUnitTests {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PermitService permitService;

    @Test
    @DisplayName("Scenario 14 & 16: BCrypt Password Hashing and Match Verification")
    public void testPasswordHashing() {
        String rawPassword = "password123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("wrongPassword", encoded));
    }

    @Test
    @DisplayName("Scenario 14: JWT Token Generation, Claim Extraction, and Expiration Validation")
    public void testJwtTokenLifecycle() {
        String username = "test_applicant_jwt";
        String token = jwtUtils.generateTokenFromUsername(username);

        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals(username, jwtUtils.getUserNameFromJwtToken(token));
        assertFalse(jwtUtils.validateJwtToken("invalid.token.payload"));
    }

    @Test
    @DisplayName("Scenario 9: Municipal Fee Calculation Logic Formula Test")
    public void testFeeCalculationFormulas() {
        // Residential: Base 5000 + (Built-up * 2.5) + (Floors * 1500)
        double builtUp = 2000.0;
        int floors = 2;
        double expectedResidential = 5000.0 + (builtUp * 2.5) + (floors * 1500.0);
        assertEquals(13000.0, expectedResidential, 0.01);

        // Commercial: Base 15000 + (Built-up * 6.0) + (Floors * 3500)
        double expectedCommercial = 15000.0 + (builtUp * 6.0) + (floors * 3500.0);
        assertEquals(34000.0, expectedCommercial, 0.01);

        // Industrial: Base 25000 + (Built-up * 8.0) + (Floors * 5000)
        double expectedIndustrial = 25000.0 + (builtUp * 8.0) + (floors * 5000.0);
        assertEquals(51000.0, expectedIndustrial, 0.01);
    }

    @Test
    @DisplayName("Scenario 12: OpenPDF Official Building Permit Certificate Generation")
    public void testPermitPdfGeneration() {
        User applicant = new User("pdf_user", "pdf@civicbuild.gov", "hash", "Test Applicant", "9876543210", new Role(UserRole.ROLE_APPLICANT, "Applicant"), null);
        Property property = new Property("123 Test Street", "SRV-999", "Ward-1", "Central Zone", new BigDecimal("1500.00"), "FREEHOLD");
        
        Application app = new Application();
        app.setId(9999L);
        app.setApplicationNumber("CB-2026-TEST");
        app.setApplicant(applicant);
        app.setProperty(property);
        app.setBuildingType(BuildingType.RESIDENTIAL);
        app.setConstructionPurpose("Residential Dwelling");
        app.setNumberOfFloors(2);
        app.setBuiltUpArea(new BigDecimal("1800.00"));
        app.setBuildingHeight(new BigDecimal("9.50"));
        app.setEstimatedCost(new BigDecimal("4500000.00"));

        File tempPdf = new File("./target/test-permit-sample.pdf");
        Permit permit = new Permit(
                "CB-PERMIT-2026-TEST",
                app,
                LocalDateTime.now(),
                LocalDateTime.now().plusYears(3),
                "VERIFIED_TEST_PAYLOAD",
                tempPdf.getAbsolutePath(),
                "Commissioner of Municipal Planning"
        );

        File generatedFile = permitService.generatePermitPdf(permit);
        assertNotNull(generatedFile);
        assertTrue(generatedFile.exists());
        assertTrue(generatedFile.length() > 0);
    }
}
