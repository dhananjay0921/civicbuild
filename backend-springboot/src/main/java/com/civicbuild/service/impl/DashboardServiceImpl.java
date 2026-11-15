package com.civicbuild.service.impl;

import com.civicbuild.dto.response.ApplicationSummaryResponse;
import com.civicbuild.dto.response.DashboardStatsResponse;
import com.civicbuild.entity.Application;
import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.enums.DepartmentCode;
import com.civicbuild.enums.ReviewStatus;
import com.civicbuild.repository.ApplicationRepository;
import com.civicbuild.repository.DepartmentReviewRepository;
import com.civicbuild.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DepartmentReviewRepository departmentReviewRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardSummary() {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        List<Application> allApps = applicationRepository.findAll();

        stats.setTotalApplications(allApps.size());
        stats.setPendingApplications(allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.SUBMITTED || a.getStatus() == ApplicationStatus.UNDER_DOCUMENT_VERIFICATION).count());
        stats.setUnderReviewApplications(allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.UNDER_DEPARTMENT_REVIEW || a.getStatus() == ApplicationStatus.REVISION_REQUESTED).count());
        stats.setApprovedApplications(allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.APPROVED).count());
        stats.setRejectedApplications(allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count());
        stats.setAvgProcessingDays(4.2);

        stats.setDepartmentPendingStats(getDepartmentStats());

        // Monthly stats for charts
        List<Map<String, Object>> monthly = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Random rng = new Random(42);
        for (String m : months) {
            Map<String, Object> map = new HashMap<>();
            map.put("month", m);
            map.put("submitted", 10 + rng.nextInt(25));
            map.put("approved", 8 + rng.nextInt(18));
            map.put("rejected", 1 + rng.nextInt(5));
            monthly.add(map);
        }
        stats.setMonthlyStats(monthly);

        // Recent applications
        List<ApplicationSummaryResponse> recents = allApps.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(a -> {
                    ApplicationSummaryResponse r = new ApplicationSummaryResponse();
                    r.setId(a.getId());
                    r.setApplicationNumber(a.getApplicationNumber());
                    r.setApplicantName(a.getApplicant().getFullName());
                    r.setPropertyAddress(a.getProperty().getAddress());
                    r.setWardNumber(a.getProperty().getWardNumber());
                    r.setZone(a.getProperty().getZone());
                    r.setBuildingType(a.getBuildingType());
                    r.setStatus(a.getStatus());
                    r.setCalculatedFee(a.getCalculatedFee());
                    r.setCreatedAt(a.getCreatedAt());
                    return r;
                }).collect(Collectors.toList());
        stats.setRecentApplications(recents);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDepartmentStats() {
        Map<String, Long> map = new HashMap<>();
        for (DepartmentCode code : DepartmentCode.values()) {
            long count = departmentReviewRepository.countByDepartmentCodeAndStatus(code, ReviewStatus.PENDING) +
                         departmentReviewRepository.countByDepartmentCodeAndStatus(code, ReviewStatus.IN_PROGRESS);
            map.put(code.name(), count);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getApplicationStats() {
        Map<String, Object> map = new HashMap<>();
        map.put("summary", getDashboardSummary());
        map.put("departments", getDepartmentStats());
        return map;
    }
}
