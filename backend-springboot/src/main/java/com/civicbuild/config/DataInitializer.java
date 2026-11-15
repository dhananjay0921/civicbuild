package com.civicbuild.config;

import com.civicbuild.entity.*;
import com.civicbuild.enums.*;
import com.civicbuild.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DepartmentReviewRepository departmentReviewRepository;

    @Autowired
    private WorkflowHistoryRepository workflowHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing CivicBuild seed roles, departments, and default municipal accounts...");

        // 1. Roles
        Role applicantRole = getOrCreateRole(UserRole.ROLE_APPLICANT, "Citizen building permit applicant");
        Role officerRole = getOrCreateRole(UserRole.ROLE_OFFICER, "Municipal department statutory review officer");
        Role adminRole = getOrCreateRole(UserRole.ROLE_ADMIN, "Municipal administrator with workflow oversight");
        Role superAdminRole = getOrCreateRole(UserRole.ROLE_SUPER_ADMIN, "Super administrator with system configuration authority");

        // 2. Departments
        getOrCreateDepartment(DepartmentCode.FIRE, "Fire Safety & Emergency Bureau", "Statutory fire resistance, egress plans, and hydrant access verification", "fire@civicbuild.gov", 5);
        getOrCreateDepartment(DepartmentCode.WATER, "Water Supply & Sewerage Board", "Municipal line connections, stormwater harvesting, and sewer capacity", "water@civicbuild.gov", 7);
        getOrCreateDepartment(DepartmentCode.ZONING, "Town Planning & Zoning Directorate", "Floor area ratio (FAR), setbacks, land-use zoning, and building height compliance", "zoning@civicbuild.gov", 10);
        getOrCreateDepartment(DepartmentCode.ENVIRONMENTAL, "Environmental Protection & Green Clearance", "EIA compliance, rainwater conservation, carbon footprint, and solar orientation", "env@civicbuild.gov", 7);

        // 3. Default Users (password: password123)
        String defaultPasswordHash = passwordEncoder.encode("password123");

        User applicantUser = getOrCreateUser("applicant1", "applicant@civicbuild.gov", defaultPasswordHash, "Aarav Sharma", "+91 98765 43210", applicantRole, null);
        getOrCreateUser("fire_officer", "fire@civicbuild.gov", defaultPasswordHash, "Chief Inspector R. Verma", "+91 98765 43211", officerRole, "FIRE");
        getOrCreateUser("water_officer", "water@civicbuild.gov", defaultPasswordHash, "Engineer S. Kulkarni", "+91 98765 43212", officerRole, "WATER");
        getOrCreateUser("zoning_officer", "zoning@civicbuild.gov", defaultPasswordHash, "Architect P. Mehta", "+91 98765 43213", officerRole, "ZONING");
        getOrCreateUser("env_officer", "env@civicbuild.gov", defaultPasswordHash, "Dr. N. Iyer", "+91 98765 43214", officerRole, "ENVIRONMENTAL");
        getOrCreateUser("admin", "admin@civicbuild.gov", defaultPasswordHash, "Municipal Administrator", "+91 98765 43215", adminRole, null);
        getOrCreateUser("superadmin", "superadmin@civicbuild.gov", defaultPasswordHash, "System Super Admin", "+91 98765 43216", superAdminRole, null);

        // 4. Seed Demonstrative Application if none exist
        if (applicationRepository.count() == 0) {
            log.info("Seeding initial demonstration building permit application...");

            Property property = new Property(
                    "Plot 42, Green Valley Avenue, Sector 8",
                    "SRV-8821/2026",
                    "Ward-14",
                    "Central Urban Zone",
                    new BigDecimal("2400.00"),
                    "FREEHOLD"
            );

            Application app = new Application();
            app.setApplicationNumber("CB-2026-1001");
            app.setApplicant(applicantUser);
            app.setProperty(property);
            app.setStatus(ApplicationStatus.UNDER_DEPARTMENT_REVIEW);
            app.setBuildingType(BuildingType.COMMERCIAL);
            app.setConstructionPurpose("Commercial Complex with Multi-Level Parking");
            app.setNumberOfFloors(4);
            app.setBuiltUpArea(new BigDecimal("7200.00"));
            app.setBuildingHeight(new BigDecimal("15.50"));
            app.setEstimatedCost(new BigDecimal("18500000.00"));
            app.setParkingAvailable(true);
            app.setProcessInstanceKey("CB-PROC-SEED-1001");
            app.setSubmissionDate(LocalDateTime.now().minusDays(2));
            app.setCalculatedFee(new BigDecimal("72200.00"));

            app = applicationRepository.save(app);

            // Seed Documents
            Document doc1 = new Document(app, DocumentType.PROPERTY_OWNERSHIP, "Title_Deed_SRV8821.pdf", "application/pdf", 1024500L, "/uploads/Title_Deed_SRV8821.pdf");
            doc1.setVerificationStatus(VerificationStatus.VERIFIED);
            documentRepository.save(doc1);

            Document doc2 = new Document(app, DocumentType.BUILDING_BLUEPRINT, "Architectural_Blueprint_v1.pdf", "application/pdf", 4521000L, "/uploads/Architectural_Blueprint_v1.pdf");
            doc2.setVerificationStatus(VerificationStatus.VERIFIED);
            documentRepository.save(doc2);

            Document doc3 = new Document(app, DocumentType.STRUCTURAL_STABILITY, "Structural_Safety_Certificate.pdf", "application/pdf", 894000L, "/uploads/Structural_Safety_Certificate.pdf");
            doc3.setVerificationStatus(VerificationStatus.VERIFIED);
            documentRepository.save(doc3);

            // Seed 4 Department Reviews
            DepartmentReview fireRev = new DepartmentReview(app, DepartmentCode.FIRE, LocalDateTime.now().plusDays(3));
            fireRev.setStatus(ReviewStatus.APPROVED);
            fireRev.setRemarks("Emergency stairs and fire hydrant pressure meets NBC 2016 norms");
            fireRev.setReviewedAt(LocalDateTime.now().minusDays(1));
            departmentReviewRepository.save(fireRev);

            DepartmentReview waterRev = new DepartmentReview(app, DepartmentCode.WATER, LocalDateTime.now().plusDays(4));
            waterRev.setStatus(ReviewStatus.APPROVED);
            waterRev.setRemarks("Sanctioned connection to 150mm municipal main water trunk");
            waterRev.setReviewedAt(LocalDateTime.now().minusHours(12));
            departmentReviewRepository.save(waterRev);

            DepartmentReview zoningRev = new DepartmentReview(app, DepartmentCode.ZONING, LocalDateTime.now().plusDays(5));
            zoningRev.setStatus(ReviewStatus.IN_PROGRESS);
            zoningRev.setRemarks("FAR calculations currently under verification against master plan");
            departmentReviewRepository.save(zoningRev);

            DepartmentReview envRev = new DepartmentReview(app, DepartmentCode.ENVIRONMENTAL, LocalDateTime.now().plusDays(5));
            envRev.setStatus(ReviewStatus.PENDING);
            departmentReviewRepository.save(envRev);

            // Seed Workflow History
            workflowHistoryRepository.save(new WorkflowHistory(app, "SUBMISSION", "APPLICATION_SUBMITTED", applicantUser.getUsername(), "Application submitted with statutory fee quotation", "CB-PROC-SEED-1001"));
            workflowHistoryRepository.save(new WorkflowHistory(app, "VALIDATION", "APPLICATION_VALIDATED", "SYSTEM", "Automated statutory boundaries and zoning checks passed", "CB-PROC-SEED-1001"));
            workflowHistoryRepository.save(new WorkflowHistory(app, "DOC_VERIFICATION", "ALL_DOCS_VERIFIED", "SYSTEM", "Ownership deed and architectural drawings verified", "CB-PROC-SEED-1001"));
            workflowHistoryRepository.save(new WorkflowHistory(app, "DEPARTMENT_REVIEW", "DECISION_APPROVED_FIRE", "fire_officer", "Fire department safety clearance issued", "CB-PROC-SEED-1001"));
            workflowHistoryRepository.save(new WorkflowHistory(app, "DEPARTMENT_REVIEW", "DECISION_APPROVED_WATER", "water_officer", "Water and sewerage NOC sanctioned", "CB-PROC-SEED-1001"));

            log.info("Sample application CB-2026-1001 seeded with live departmental reviews and documents!");
        }

        log.info("CivicBuild backend startup bootstrap sequence completed successfully.");
    }

    private Role getOrCreateRole(UserRole roleEnum, String description) {
        return roleRepository.findByName(roleEnum)
                .orElseGet(() -> roleRepository.save(new Role(roleEnum, description)));
    }

    private Department getOrCreateDepartment(DepartmentCode code, String name, String desc, String email, int sla) {
        return departmentRepository.findByCode(code)
                .orElseGet(() -> departmentRepository.save(new Department(code, name, desc, email, sla)));
    }

    private User getOrCreateUser(String username, String email, String password, String fullName, String phone, Role role, String deptCode) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new User(username, email, password, fullName, phone, role, deptCode)));
    }
}
