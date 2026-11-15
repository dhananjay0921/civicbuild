package com.civicbuild.service.impl;

import com.civicbuild.entity.Application;
import com.civicbuild.entity.RevisionRequest;
import com.civicbuild.entity.User;
import com.civicbuild.enums.RevisionStatus;
import com.civicbuild.enums.UserRole;
import com.civicbuild.exception.ResourceNotFoundException;
import com.civicbuild.exception.UnauthorizedException;
import com.civicbuild.repository.RevisionRequestRepository;
import com.civicbuild.service.RevisionService;
import com.civicbuild.service.WorkflowOrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RevisionServiceImpl implements RevisionService {

    @Autowired
    private RevisionRequestRepository revisionRequestRepository;

    @Autowired
    private WorkflowOrchestrationService workflowOrchestrationService;

    @Override
    @Transactional(readOnly = true)
    public RevisionRequest getRevisionById(Long id, User user) {
        RevisionRequest req = revisionRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revision request not found: " + id));

        validateAccess(req.getApplication(), user);
        return req;
    }

    @Override
    @Transactional
    public RevisionRequest submitRevision(Long id, String remarks, User user) {
        RevisionRequest req = getRevisionById(id, user);

        req.setStatus(RevisionStatus.SUBMITTED);
        req.setRespondedAt(LocalDateTime.now());
        req = revisionRequestRepository.save(req);

        workflowOrchestrationService.triggerRevisionSubmitted(req.getApplication().getId());
        return req;
    }

    private void validateAccess(Application application, User user) {
        if (user.getRole().getName() == UserRole.ROLE_ADMIN ||
            user.getRole().getName() == UserRole.ROLE_SUPER_ADMIN ||
            user.getRole().getName() == UserRole.ROLE_OFFICER) {
            return;
        }
        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to view or respond to this revision");
        }
    }
}
