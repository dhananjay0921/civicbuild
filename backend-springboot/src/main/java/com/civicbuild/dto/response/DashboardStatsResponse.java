package com.civicbuild.dto.response;

import java.util.List;
import java.util.Map;

public class DashboardStatsResponse {
    private long totalApplications;
    private long pendingApplications;
    private long approvedApplications;
    private long rejectedApplications;
    private long underReviewApplications;
    private double avgProcessingDays;
    private Map<String, Long> departmentPendingStats;
    private List<Map<String, Object>> monthlyStats;
    private List<ApplicationSummaryResponse> recentApplications;

    public DashboardStatsResponse() {}

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getPendingApplications() { return pendingApplications; }
    public void setPendingApplications(long pendingApplications) { this.pendingApplications = pendingApplications; }

    public long getApprovedApplications() { return approvedApplications; }
    public void setApprovedApplications(long approvedApplications) { this.approvedApplications = approvedApplications; }

    public long getRejectedApplications() { return rejectedApplications; }
    public void setRejectedApplications(long rejectedApplications) { this.rejectedApplications = rejectedApplications; }

    public long getUnderReviewApplications() { return underReviewApplications; }
    public void setUnderReviewApplications(long underReviewApplications) { this.underReviewApplications = underReviewApplications; }

    public double getAvgProcessingDays() { return avgProcessingDays; }
    public void setAvgProcessingDays(double avgProcessingDays) { this.avgProcessingDays = avgProcessingDays; }

    public Map<String, Long> getDepartmentPendingStats() { return departmentPendingStats; }
    public void setDepartmentPendingStats(Map<String, Long> departmentPendingStats) { this.departmentPendingStats = departmentPendingStats; }

    public List<Map<String, Object>> getMonthlyStats() { return monthlyStats; }
    public void setMonthlyStats(List<Map<String, Object>> monthlyStats) { this.monthlyStats = monthlyStats; }

    public List<ApplicationSummaryResponse> getRecentApplications() { return recentApplications; }
    public void setRecentApplications(List<ApplicationSummaryResponse> recentApplications) { this.recentApplications = recentApplications; }
}
