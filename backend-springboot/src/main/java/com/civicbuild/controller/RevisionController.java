package com.civicbuild.controller;

import com.civicbuild.dto.request.RevisionSubmitRequest;
import com.civicbuild.dto.response.ApiResponse;
import com.civicbuild.entity.RevisionRequest;
import com.civicbuild.entity.User;
import com.civicbuild.service.AuthService;
import com.civicbuild.service.RevisionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/revisions")
public class RevisionController {

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private AuthService authService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RevisionRequest>> getRevisionById(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        RevisionRequest revision = revisionService.getRevisionById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Revision request retrieved", revision));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<RevisionRequest>> submitRevision(
            @PathVariable Long id,
            @Valid @RequestBody RevisionSubmitRequest request) {
        User currentUser = authService.getCurrentUser();
        RevisionRequest revision = revisionService.submitRevision(id, request.getRemarks(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("Revision response submitted successfully", revision));
    }
}
