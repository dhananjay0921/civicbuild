package com.civicbuild.service;

import com.civicbuild.dto.request.UserCreateUpdateRequest;
import com.civicbuild.entity.AuditLog;
import com.civicbuild.entity.User;
import java.util.List;
import java.util.Map;

public interface AdminService {
    List<User> getAllUsers();
    User createUser(UserCreateUpdateRequest request);
    User updateUser(Long id, UserCreateUpdateRequest request);
    List<Map<String, Object>> getWorkflowInstances();
    List<AuditLog> getAuditLogs();
}
