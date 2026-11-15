package com.civicbuild.service;

import com.civicbuild.dto.response.DashboardStatsResponse;
import java.util.Map;

public interface DashboardService {
    DashboardStatsResponse getDashboardSummary();
    Map<String, Long> getDepartmentStats();
    Map<String, Object> getApplicationStats();
}
