package com.civicbuild.controller;

import com.civicbuild.dto.request.ReviewDecisionRequest;
import com.civicbuild.dto.response.ApiResponse;
import com.civicbuild.entity.Department;
import com.civicbuild.entity.DepartmentReview;
import com.civicbuild.entity.User;
import com.civicbuild.repository.DepartmentRepository;
import com.civicbuild.service.AuthService;
import com.civicbuild.service.DepartmentReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class DepartmentReviewController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentReviewService departmentReviewService;

    @Autowired
    private AuthService authService;

    @GetMapping("/api/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved", departments));
    }

    @GetMapping("/api/reviews")
    @PreAuthorize("hasAnyRole('ROLE_OFFICER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentReview>>> getAssignedReviews() {
        User currentUser = authService.getCurrentUser();
        List<DepartmentReview> reviews = departmentReviewService.getAssignedReviews(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Assigned reviews retrieved", reviews));
    }

    @GetMapping("/api/reviews/{id}")
    @PreAuthorize("hasAnyRole('ROLE_OFFICER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentReview>> getReviewById(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        DepartmentReview review = departmentReviewService.getReviewById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Review details retrieved", review));
    }

    @PostMapping("/api/reviews/{id}/approve")
    @PreAuthorize("hasAnyRole('ROLE_OFFICER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentReview>> approveReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDecisionRequest request) {
        User currentUser = authService.getCurrentUser();
        DepartmentReview review = departmentReviewService.approveReview(id, request.getRemarks(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("Department clearance APPROVED successfully", review));
    }

    @PostMapping("/api/reviews/{id}/reject")
    @PreAuthorize("hasAnyRole('ROLE_OFFICER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentReview>> rejectReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDecisionRequest request) {
        User currentUser = authService.getCurrentUser();
        DepartmentReview review = departmentReviewService.rejectReview(id, request.getRemarks(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("Department clearance REJECTED", review));
    }

    @PostMapping("/api/reviews/{id}/request-revision")
    @PreAuthorize("hasAnyRole('ROLE_OFFICER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentReview>> requestRevision(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDecisionRequest request) {
        User currentUser = authService.getCurrentUser();
        DepartmentReview review = departmentReviewService.requestRevision(id, request.getRemarks(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("Revision requested from applicant", review));
    }
}
