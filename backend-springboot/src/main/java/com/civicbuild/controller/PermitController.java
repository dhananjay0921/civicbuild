package com.civicbuild.controller;

import com.civicbuild.dto.response.ApiResponse;
import com.civicbuild.entity.Permit;
import com.civicbuild.entity.User;
import com.civicbuild.repository.PermitRepository;
import com.civicbuild.service.AuthService;
import com.civicbuild.service.PermitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/permits")
public class PermitController {

    @Autowired
    private PermitService permitService;

    @Autowired
    private PermitRepository permitRepository;

    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Permit>>> getAllPermits() {
        List<Permit> permits = permitRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Permits retrieved", permits));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Permit>> getPermitById(@PathVariable Long id) {
        Permit permit = permitService.getPermitById(id);
        return ResponseEntity.ok(ApiResponse.success("Permit details retrieved", permit));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPermitPdf(@PathVariable Long id) {
        Permit permit = permitService.getPermitById(id);
        File pdfFile = new File(permit.getPdfPath());

        // Ensure physical file exists or re-generate
        if (!pdfFile.exists()) {
            pdfFile = permitService.generatePermitPdf(permit);
        }

        Resource resource = new FileSystemResource(pdfFile);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + permit.getPermitNumber() + ".pdf\"")
                .body(resource);
    }
}
