package com.civicbuild.service.impl;

import com.civicbuild.dto.request.ApplicationCreateRequest;
import com.civicbuild.dto.response.ApplicationDetailResponse;
import com.civicbuild.dto.response.ApplicationSummaryResponse;
import com.civicbuild.entity.*;
import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.enums.DocumentType;
import com.civicbuild.enums.UserRole;
import com.civicbuild.enums.VerificationStatus;
import com.civicbuild.exception.BadRequestException;
import com.civicbuild.exception.ResourceNotFoundException;
import com.civicbuild.exception.UnauthorizedException;
import com.civicbuild.repository.*;
import com.civicbuild.service.ApplicationService;
import com.civicbuild.service.WorkflowOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private WorkflowHistoryRepository workflowHistoryRepository;

    @Autowired
    private WorkflowOrchestrationService workflowOrchestrationService;

    @Value("${civicbuild.storage.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public ApplicationDetailResponse createDraft(ApplicationCreateRequest request, User applicant) {
        Property property = new Property(
                request.getAddress(),
                request.getSurveyNumber(),
                request.getWardNumber(),
                request.getZone(),
                request.getPlotArea(),
                request.getOwnershipType()
        );

        String applicationNumber = "CB-" + LocalDateTime.now().getYear() + "-" + (1000 + new Random().nextInt(9000));

        Application application = new Application();
        application.setApplicationNumber(applicationNumber);
        application.setApplicant(applicant);
        application.setProperty(property);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setBuildingType(request.getBuildingType());
        application.setConstructionPurpose(request.getConstructionPurpose());
        application.setNumberOfFloors(request.getNumberOfFloors());
        application.setBuiltUpArea(request.getBuiltUpArea());
        application.setBuildingHeight(request.getBuildingHeight());
        application.setEstimatedCost(request.getEstimatedCost());
        application.setParkingAvailable(request.getParkingAvailable() != null ? request.getParkingAvailable() : true);

        application = applicationRepository.save(application);

        workflowHistoryRepository.save(new WorkflowHistory(
                application, "DRAFT", "APPLICATION_DRAFT_CREATED", applicant.getUsername(),
                "Draft application initiated by applicant", null
        ));

        return mapToDetailResponse(application);
    }

    @Override
    @Transactional
    public ApplicationDetailResponse updateDraft(Long id, ApplicationCreateRequest request, User applicant) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        validateOwnershipOrAdmin(application, applicant);

        if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.REVISION_REQUESTED) {
            throw new BadRequestException("Only DRAFT or REVISION_REQUESTED applications can be modified");
        }

        Property prop = application.getProperty();
        prop.setAddress(request.getAddress());
        prop.setSurveyNumber(request.getSurveyNumber());
        prop.setWardNumber(request.getWardNumber());
        prop.setZone(request.getZone());
        prop.setPlotArea(request.getPlotArea());
        prop.setOwnershipType(request.getOwnershipType());

        application.setBuildingType(request.getBuildingType());
        application.setConstructionPurpose(request.getConstructionPurpose());
        application.setNumberOfFloors(request.getNumberOfFloors());
        application.setBuiltUpArea(request.getBuiltUpArea());
        application.setBuildingHeight(request.getBuildingHeight());
        application.setEstimatedCost(request.getEstimatedCost());
        application.setParkingAvailable(request.getParkingAvailable());

        application = applicationRepository.save(application);
        return mapToDetailResponse(application);
    }

    @Override
    @Transactional
    public Document uploadDocument(Long applicationId, MultipartFile file, DocumentType documentType, User user) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        validateOwnershipOrAdmin(application, user);

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : ".pdf";
            String uniqueFileName = "APP_" + applicationId + "_" + documentType.name() + "_" + System.currentTimeMillis() + extension;
            Path targetPath = Paths.get(uploadDir).resolve(uniqueFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            Optional<Document> existingOpt = documentRepository.findByApplicationIdAndDocumentType(applicationId, documentType);
            Document doc;
            if (existingOpt.isPresent()) {
                doc = existingOpt.get();
                doc.setFileName(originalName);
                doc.setFileType(file.getContentType() != null ? file.getContentType() : "application/pdf");
                doc.setFileSize(file.getSize());
                doc.setFilePath(targetPath.toAbsolutePath().toString());
                doc.setVerificationStatus(VerificationStatus.PENDING);
                doc.setUploadedAt(LocalDateTime.now());
            } else {
                doc = new Document(
                        application,
                        documentType,
                        originalName,
                        file.getContentType() != null ? file.getContentType() : "application/pdf",
                        file.getSize(),
                        targetPath.toAbsolutePath().toString()
                );
            }

            doc = documentRepository.save(doc);

            workflowHistoryRepository.save(new WorkflowHistory(
                    application, "DOCUMENT_UPLOAD", "DOCUMENT_ATTACHED", user.getUsername(),
                    "Document uploaded: " + documentType.name() + " (" + originalName + ")",
                    application.getProcessInstanceKey()
            ));

            return doc;
        } catch (IOException e) {
            log.error("Failed to store file for application ID {}: {}", applicationId, e.getMessage());
            throw new BadRequestException("Could not store file: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApplicationDetailResponse submitApplication(Long id, User applicant) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        validateOwnershipOrAdmin(application, applicant);

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new BadRequestException("Application has already been submitted");
        }

        List<Document> docs = documentRepository.findByApplicationId(id);
        if (docs.isEmpty()) {
            throw new BadRequestException("Please upload statutory documentation (Blueprint, Ownership Proof) before submission");
        }

        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmissionDate(LocalDateTime.now());
        application = applicationRepository.save(application);

        workflowHistoryRepository.save(new WorkflowHistory(
                application, "SUBMISSION", "APPLICATION_SUBMITTED", applicant.getUsername(),
                "Application formally submitted for municipal verification", null
        ));

        // Start Camunda 8 Workflow Process!
        String procKey = workflowOrchestrationService.startProcessInstance(application);
        application.setProcessInstanceKey(procKey);
        application = applicationRepository.save(application);

        return mapToDetailResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDetailResponse getApplicationById(Long id, User user) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        validateAccess(application, user);
        return mapToDetailResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationSummaryResponse> getMyApplications(User applicant) {
        return applicationRepository.findByApplicantId(applicant.getId())
                .stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationSummaryResponse> getAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getApplicationStatus(Long id, User user) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        validateAccess(application, user);

        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("id", application.getId());
        statusMap.put("applicationNumber", application.getApplicationNumber());
        statusMap.put("status", application.getStatus().name());
        statusMap.put("processInstanceKey", application.getProcessInstanceKey());
        statusMap.put("currentStage", mapStatusToStage(application.getStatus()));
        statusMap.put("reviews", application.getDepartmentReviews());
        statusMap.put("calculatedFee", application.getCalculatedFee());
        statusMap.put("permitIssued", application.getPermit() != null);
        return statusMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowHistory> getApplicationHistory(Long id, User user) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        validateAccess(application, user);
        return workflowHistoryRepository.findByApplicationIdOrderByTimestampAsc(id);
    }

    private void validateOwnershipOrAdmin(Application application, User user) {
        if (user.getRole().getName() == UserRole.ROLE_ADMIN || user.getRole().getName() == UserRole.ROLE_SUPER_ADMIN) {
            return;
        }
        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to modify this application");
        }
    }

    private void validateAccess(Application application, User user) {
        if (user.getRole().getName() == UserRole.ROLE_ADMIN ||
            user.getRole().getName() == UserRole.ROLE_SUPER_ADMIN ||
            user.getRole().getName() == UserRole.ROLE_OFFICER) {
            return;
        }
        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to view this application");
        }
    }

    private int mapStatusToStage(ApplicationStatus status) {
        return switch (status) {
            case DRAFT -> 1;
            case SUBMITTED -> 1;
            case UNDER_DOCUMENT_VERIFICATION -> 2;
            case UNDER_DEPARTMENT_REVIEW -> 3;
            case REVISION_REQUESTED -> 4;
            case FEE_PENDING -> 5;
            case PAYMENT_PENDING -> 6;
            case APPROVED -> 7;
            case REJECTED -> 0;
        };
    }

    private ApplicationSummaryResponse mapToSummaryResponse(Application app) {
        ApplicationSummaryResponse dto = new ApplicationSummaryResponse();
        dto.setId(app.getId());
        dto.setApplicationNumber(app.getApplicationNumber());
        dto.setApplicantName(app.getApplicant().getFullName());
        dto.setApplicantEmail(app.getApplicant().getEmail());
        dto.setPropertyAddress(app.getProperty().getAddress());
        dto.setWardNumber(app.getProperty().getWardNumber());
        dto.setZone(app.getProperty().getZone());
        dto.setBuildingType(app.getBuildingType());
        dto.setNumberOfFloors(app.getNumberOfFloors());
        dto.setBuiltUpArea(app.getBuiltUpArea());
        dto.setStatus(app.getStatus());
        dto.setCalculatedFee(app.getCalculatedFee());
        dto.setProcessInstanceKey(app.getProcessInstanceKey());
        dto.setSubmissionDate(app.getSubmissionDate());
        dto.setCreatedAt(app.getCreatedAt());
        return dto;
    }

    private ApplicationDetailResponse mapToDetailResponse(Application app) {
        ApplicationDetailResponse dto = new ApplicationDetailResponse();
        dto.setId(app.getId());
        dto.setApplicationNumber(app.getApplicationNumber());
        dto.setApplicant(app.getApplicant());
        dto.setProperty(app.getProperty());
        dto.setStatus(app.getStatus());
        dto.setBuildingType(app.getBuildingType());
        dto.setConstructionPurpose(app.getConstructionPurpose());
        dto.setNumberOfFloors(app.getNumberOfFloors());
        dto.setBuiltUpArea(app.getBuiltUpArea());
        dto.setBuildingHeight(app.getBuildingHeight());
        dto.setEstimatedCost(app.getEstimatedCost());
        dto.setParkingAvailable(app.getParkingAvailable());
        dto.setProcessInstanceKey(app.getProcessInstanceKey());
        dto.setSubmissionDate(app.getSubmissionDate());
        dto.setApprovedDate(app.getApprovedDate());
        dto.setRejectedDate(app.getRejectedDate());
        dto.setRejectionReason(app.getRejectionReason());
        dto.setCalculatedFee(app.getCalculatedFee());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setUpdatedAt(app.getUpdatedAt());

        dto.setDocuments(documentRepository.findByApplicationId(app.getId()));
        dto.setDepartmentReviews(app.getDepartmentReviews());
        dto.setRevisionRequests(app.getRevisionRequests());
        dto.setPayments(app.getPayments());
        dto.setWorkflowHistories(workflowHistoryRepository.findByApplicationIdOrderByTimestampAsc(app.getId()));
        dto.setPermit(app.getPermit());
        return dto;
    }
}
