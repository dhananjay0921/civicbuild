package com.civicbuild.service.impl;

import com.civicbuild.dto.response.TaskItemResponse;
import com.civicbuild.entity.Application;
import com.civicbuild.entity.DepartmentReview;
import com.civicbuild.entity.User;
import com.civicbuild.enums.ApplicationStatus;
import com.civicbuild.enums.DepartmentCode;
import com.civicbuild.enums.ReviewStatus;
import com.civicbuild.enums.UserRole;
import com.civicbuild.exception.ResourceNotFoundException;
import com.civicbuild.repository.ApplicationRepository;
import com.civicbuild.repository.DepartmentReviewRepository;
import com.civicbuild.service.DepartmentReviewService;
import com.civicbuild.service.TaskService;
import com.civicbuild.service.WorkflowOrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private DepartmentReviewRepository departmentReviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DepartmentReviewService departmentReviewService;

    @Autowired
    private WorkflowOrchestrationService workflowOrchestrationService;

    @Override
    @Transactional(readOnly = true)
    public List<TaskItemResponse> getMyTasks(User user) {
        List<TaskItemResponse> tasks = new ArrayList<>();

        if (user.getRole().getName() == UserRole.ROLE_OFFICER) {
            DepartmentCode deptCode = null;
            if (user.getDepartmentCode() != null) {
                try {
                    deptCode = DepartmentCode.valueOf(user.getDepartmentCode());
                } catch (Exception ignored) {}
            }

            List<DepartmentReview> pendingReviews;
            if (deptCode != null) {
                pendingReviews = departmentReviewRepository.findByDepartmentCode(deptCode);
            } else {
                pendingReviews = departmentReviewRepository.findAll();
            }

            for (DepartmentReview rev : pendingReviews) {
                if (rev.getStatus() == ReviewStatus.PENDING || rev.getStatus() == ReviewStatus.IN_PROGRESS) {
                    TaskItemResponse task = new TaskItemResponse();
                    task.setTaskId("REV-" + rev.getId());
                    task.setTaskName("Department Statutory Review (" + rev.getDepartmentCode().name() + ")");
                    task.setApplicationId(rev.getApplication().getId());
                    task.setApplicationNumber(rev.getApplication().getApplicationNumber());
                    task.setApplicantName(rev.getApplication().getApplicant().getFullName());
                    task.setBuildingType(rev.getApplication().getBuildingType().name());
                    task.setDepartment(rev.getDepartmentCode().name());
                    task.setStatus(rev.getStatus().name());
                    task.setCreatedDate(rev.getCreatedAt());
                    task.setDueDate(rev.getDueDate());

                    Map<String, Object> vars = new HashMap<>();
                    vars.put("departmentReviewId", rev.getId());
                    vars.put("plotArea", rev.getApplication().getProperty().getPlotArea());
                    vars.put("builtUpArea", rev.getApplication().getBuiltUpArea());
                    vars.put("numberOfFloors", rev.getApplication().getNumberOfFloors());
                    task.setTaskVariables(vars);

                    tasks.add(task);
                }
            }
        } else if (user.getRole().getName() == UserRole.ROLE_APPLICANT) {
            List<Application> myApps = applicationRepository.findByApplicantId(user.getId());
            for (Application app : myApps) {
                if (app.getStatus() == ApplicationStatus.REVISION_REQUESTED) {
                    TaskItemResponse task = new TaskItemResponse();
                    task.setTaskId("APP-REV-" + app.getId());
                    task.setTaskName("Submit Corrected Architectural Blueprint");
                    task.setApplicationId(app.getId());
                    task.setApplicationNumber(app.getApplicationNumber());
                    task.setApplicantName(user.getFullName());
                    task.setBuildingType(app.getBuildingType().name());
                    task.setDepartment("APPLICANT_ACTION");
                    task.setStatus("REVISION_PENDING");
                    task.setCreatedDate(app.getUpdatedAt());
                    task.setDueDate(app.getUpdatedAt().plusDays(14));
                    tasks.add(task);
                } else if (app.getStatus() == ApplicationStatus.PAYMENT_PENDING || app.getStatus() == ApplicationStatus.FEE_PENDING) {
                    TaskItemResponse task = new TaskItemResponse();
                    task.setTaskId("APP-PAY-" + app.getId());
                    task.setTaskName("Pay Statutory Municipal Building Permit Fees");
                    task.setApplicationId(app.getId());
                    task.setApplicationNumber(app.getApplicationNumber());
                    task.setApplicantName(user.getFullName());
                    task.setBuildingType(app.getBuildingType().name());
                    task.setDepartment("FINANCE");
                    task.setStatus("PAYMENT_PENDING");
                    task.setCreatedDate(app.getUpdatedAt());
                    task.setDueDate(app.getUpdatedAt().plusDays(30));

                    Map<String, Object> vars = new HashMap<>();
                    vars.put("feeAmount", app.getCalculatedFee());
                    task.setTaskVariables(vars);

                    tasks.add(task);
                }
            }
        } else if (user.getRole().getName() == UserRole.ROLE_ADMIN || user.getRole().getName() == UserRole.ROLE_SUPER_ADMIN) {
            List<DepartmentReview> all = departmentReviewRepository.findAll();
            for (DepartmentReview rev : all) {
                if (rev.getStatus() == ReviewStatus.PENDING || rev.getStatus() == ReviewStatus.IN_PROGRESS) {
                    TaskItemResponse task = new TaskItemResponse();
                    task.setTaskId("REV-" + rev.getId());
                    task.setTaskName("Department Review: " + rev.getDepartmentCode().name());
                    task.setApplicationId(rev.getApplication().getId());
                    task.setApplicationNumber(rev.getApplication().getApplicationNumber());
                    task.setApplicantName(rev.getApplication().getApplicant().getFullName());
                    task.setBuildingType(rev.getApplication().getBuildingType().name());
                    task.setDepartment(rev.getDepartmentCode().name());
                    task.setStatus(rev.getStatus().name());
                    task.setCreatedDate(rev.getCreatedAt());
                    task.setDueDate(rev.getDueDate());
                    tasks.add(task);
                }
            }
        }

        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public TaskItemResponse getTaskById(String taskId, User user) {
        return getMyTasks(user).stream()
                .filter(t -> t.getTaskId().equalsIgnoreCase(taskId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Task not found or unauthorized: " + taskId));
    }

    @Override
    @Transactional
    public void completeTask(String taskId, Map<String, Object> variables, User user) {
        if (taskId.startsWith("REV-")) {
            Long revId = Long.parseLong(taskId.substring(4));
            String decision = (String) variables.getOrDefault("status", "APPROVED");
            String remarks = (String) variables.getOrDefault("remarks", "Review submitted through Tasklist");
            if ("APPROVED".equalsIgnoreCase(decision)) {
                departmentReviewService.approveReview(revId, remarks, user);
            } else if ("REJECTED".equalsIgnoreCase(decision)) {
                departmentReviewService.rejectReview(revId, remarks, user);
            } else if ("REVISION_REQUESTED".equalsIgnoreCase(decision)) {
                departmentReviewService.requestRevision(revId, remarks, user);
            }
        } else if (taskId.startsWith("APP-REV-")) {
            Long appId = Long.parseLong(taskId.substring(8));
            workflowOrchestrationService.triggerRevisionSubmitted(appId);
        } else if (taskId.startsWith("APP-PAY-")) {
            Long appId = Long.parseLong(taskId.substring(8));
            workflowOrchestrationService.triggerPaymentConfirmed(appId);
        }

        workflowOrchestrationService.completeUserTask(taskId, variables);
    }
}
