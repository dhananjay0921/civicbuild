package com.civicbuild.controller;

import com.civicbuild.dto.response.ApiResponse;
import com.civicbuild.dto.response.DashboardStatsResponse;
import com.civicbuild.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getSummary() {
        DashboardStatsResponse summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved", summary));
    }

    @GetMapping("/department-stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDepartmentStats() {
        Map<String, Long> stats = dashboardService.getDepartmentStats();
        return ResponseEntity.ok(ApiResponse.success("Department statistics retrieved", stats));
    }

    @GetMapping("/application-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApplicationStats() {
        Map<String, Object> stats = dashboardService.getApplicationStats();
        return ResponseEntity.ok(ApiResponse.success("Application statistics retrieved", stats));
    }
}
