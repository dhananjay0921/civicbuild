package com.civicbuild.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class TaskItemResponse {
    private String taskId;
    private String taskName;
    private Long applicationId;
    private String applicationNumber;
    private String applicantName;
    private String buildingType;
    private String department;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime dueDate;
    private Map<String, Object> taskVariables;

    public TaskItemResponse() {}

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getBuildingType() { return buildingType; }
    public void setBuildingType(String buildingType) { this.buildingType = buildingType; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Map<String, Object> getTaskVariables() { return taskVariables; }
    public void setTaskVariables(Map<String, Object> taskVariables) { this.taskVariables = taskVariables; }
}
