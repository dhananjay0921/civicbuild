package com.civicbuild.service.impl;

import com.civicbuild.dto.request.UserCreateUpdateRequest;
import com.civicbuild.entity.Application;
import com.civicbuild.entity.AuditLog;
import com.civicbuild.entity.Role;
import com.civicbuild.entity.User;
import com.civicbuild.exception.BadRequestException;
import com.civicbuild.exception.ResourceNotFoundException;
import com.civicbuild.repository.ApplicationRepository;
import com.civicbuild.repository.AuditLogRepository;
import com.civicbuild.repository.RoleRepository;
import com.civicbuild.repository.UserRepository;
import com.civicbuild.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User createUser(UserCreateUpdateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already in use");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseGet(() -> roleRepository.save(new Role(request.getRole(), request.getRole().name())));

        String encodedPassword = passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "password123");
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                encodedPassword,
                request.getFullName(),
                request.getPhone(),
                role,
                request.getDepartmentCode()
        );
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        user = userRepository.save(user);

        auditLogRepository.save(new AuditLog(
                null, "USER_CREATED", "USER", user.getId().toString(),
                "127.0.0.1", "Admin created user " + user.getUsername() + " (" + user.getRole().getName() + ")"
        ));

        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserCreateUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        if (request.getDepartmentCode() != null) {
            user.setDepartmentCode(request.getDepartmentCode());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getRole() != null) {
            Role role = roleRepository.findByName(request.getRole())
                    .orElseGet(() -> roleRepository.save(new Role(request.getRole(), request.getRole().name())));
            user.setRole(role);
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);

        auditLogRepository.save(new AuditLog(
                null, "USER_UPDATED", "USER", user.getId().toString(),
                "127.0.0.1", "Admin updated profile for " + user.getUsername()
        ));

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWorkflowInstances() {
        List<Application> apps = applicationRepository.findAll();
        List<Map<String, Object>> workflows = new ArrayList<>();

        for (Application app : apps) {
            Map<String, Object> wf = new HashMap<>();
            wf.put("processInstanceKey", app.getProcessInstanceKey() != null ? app.getProcessInstanceKey() : "N/A");
            wf.put("bpmnProcessId", "BuildingPermitApprovalProcess");
            wf.put("applicationId", app.getId());
            wf.put("applicationNumber", app.getApplicationNumber());
            wf.put("applicantName", app.getApplicant().getFullName());
            wf.put("status", app.getStatus().name());
            wf.put("startDate", app.getSubmissionDate() != null ? app.getSubmissionDate() : app.getCreatedAt());
            wf.put("endDate", app.getApprovedDate() != null ? app.getApprovedDate() : app.getRejectedDate());
            wf.put("reviews", app.getDepartmentReviews());
            workflows.add(wf);
        }
        return workflows;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
