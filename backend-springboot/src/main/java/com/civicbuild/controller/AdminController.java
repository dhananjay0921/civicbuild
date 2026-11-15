package com.civicbuild.controller;

import com.civicbuild.dto.request.UserCreateUpdateRequest;
import com.civicbuild.dto.response.ApiResponse;
import com.civicbuild.entity.AuditLog;
import com.civicbuild.entity.User;
import com.civicbuild.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserCreateUpdateRequest request) {
        User user = adminService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("User provisioned successfully", user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserCreateUpdateRequest request) {
        User user = adminService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @GetMapping("/workflows")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWorkflows() {
        List<Map<String, Object>> workflows = adminService.getWorkflowInstances();
        return ResponseEntity.ok(ApiResponse.success("Workflows retrieved", workflows));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        List<AuditLog> logs = adminService.getAuditLogs();
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", logs));
    }
}
