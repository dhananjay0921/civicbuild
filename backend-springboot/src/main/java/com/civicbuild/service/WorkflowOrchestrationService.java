package com.civicbuild.service;

import com.civicbuild.entity.Application;
import java.util.Map;

public interface WorkflowOrchestrationService {
    String startProcessInstance(Application application);
    void completeUserTask(String taskId, Map<String, Object> variables);
    void triggerReviewDecision(Long applicationId, String departmentCode, String decision, String remarks);
    void triggerRevisionSubmitted(Long applicationId);
    void triggerPaymentConfirmed(Long applicationId);
}
