package com.civicbuild.controller;

import com.civicbuild.dto.request.ApplicationCreateRequest;
import com.civicbuild.dto.response.ApiResponse;
import com.civicbuild.dto.response.ApplicationDetailResponse;
import com.civicbuild.dto.response.ApplicationSummaryResponse;
import com.civicbuild.entity.Document;
import com.civicbuild.entity.User;
import com.civicbuild.entity.WorkflowHistory;
import com.civicbuild.enums.DocumentType;
import com.civicbuild.enums.UserRole;
import com.civicbuild.service.ApplicationService;
import com.civicbuild.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> createApplication(
            @Valid @RequestBody ApplicationCreateRequest request) {
        User currentUser = authService.getCurrentUser();
        ApplicationDetailResponse response = applicationService.createDraft(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Draft application created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApplicationSummaryResponse>>> getApplications() {
        User currentUser = authService.getCurrentUser();
        List<ApplicationSummaryResponse> list;
        if (currentUser.getRole().getName() == UserRole.ROLE_APPLICANT) {
            list = applicationService.getMyApplications(currentUser);
        } else {
            list = applicationService.getAllApplications();
        }
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getApplicationById(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        ApplicationDetailResponse response = applicationService.getApplicationById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Application details retrieved", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationCreateRequest request) {
        User currentUser = authService.getCurrentUser();
        ApplicationDetailResponse response = applicationService.updateDraft(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Application updated successfully", response));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> submitApplication(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        ApplicationDetailResponse response = applicationService.submitApplication(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully and workflow initiated", response));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<Document>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType) {
        User currentUser = authService.getCurrentUser();
        Document document = applicationService.uploadDocument(id, file, documentType, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", document));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApplicationStatus(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        Map<String, Object> status = applicationService.getApplicationStatus(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Application workflow status retrieved", status));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<WorkflowHistory>>> getApplicationHistory(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        List<WorkflowHistory> history = applicationService.getApplicationHistory(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Workflow audit history retrieved", history));
    }
}
