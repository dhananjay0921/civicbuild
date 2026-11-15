package com.civicbuild.service.impl;

import com.civicbuild.entity.DepartmentReview;
import com.civicbuild.entity.User;
import com.civicbuild.enums.DepartmentCode;
import com.civicbuild.enums.ReviewStatus;
import com.civicbuild.enums.UserRole;
import com.civicbuild.exception.ResourceNotFoundException;
import com.civicbuild.exception.UnauthorizedException;
import com.civicbuild.repository.DepartmentReviewRepository;
import com.civicbuild.service.DepartmentReviewService;
import com.civicbuild.service.WorkflowOrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentReviewServiceImpl implements DepartmentReviewService {

    @Autowired
    private DepartmentReviewRepository departmentReviewRepository;

    @Autowired
    private WorkflowOrchestrationService workflowOrchestrationService;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentReview> getAssignedReviews(User officer) {
        if (officer.getRole().getName() == UserRole.ROLE_ADMIN || officer.getRole().getName() == UserRole.ROLE_SUPER_ADMIN) {
            return departmentReviewRepository.findAll();
        }

        if (officer.getDepartmentCode() != null) {
            try {
                DepartmentCode code = DepartmentCode.valueOf(officer.getDepartmentCode());
                return departmentReviewRepository.findByDepartmentCode(code);
            } catch (Exception ignored) {}
        }

        return departmentReviewRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReview getReviewById(Long id, User officer) {
        DepartmentReview review = departmentReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department review not found: " + id));

        validateOfficerAccess(review, officer);
        return review;
    }

    @Override
    @Transactional
    public DepartmentReview approveReview(Long id, String remarks, User officer) {
        DepartmentReview review = getReviewById(id, officer);
        review.setOfficer(officer);
        workflowOrchestrationService.triggerReviewDecision(
                review.getApplication().getId(),
                review.getDepartmentCode().name(),
                ReviewStatus.APPROVED.name(),
                remarks
        );
        return departmentReviewRepository.findById(id).orElse(review);
    }

    @Override
    @Transactional
    public DepartmentReview rejectReview(Long id, String remarks, User officer) {
        DepartmentReview review = getReviewById(id, officer);
        review.setOfficer(officer);
        workflowOrchestrationService.triggerReviewDecision(
                review.getApplication().getId(),
                review.getDepartmentCode().name(),
                ReviewStatus.REJECTED.name(),
                remarks
        );
        return departmentReviewRepository.findById(id).orElse(review);
    }

    @Override
    @Transactional
    public DepartmentReview requestRevision(Long id, String remarks, User officer) {
        DepartmentReview review = getReviewById(id, officer);
        review.setOfficer(officer);
        workflowOrchestrationService.triggerReviewDecision(
                review.getApplication().getId(),
                review.getDepartmentCode().name(),
                ReviewStatus.REVISION_REQUESTED.name(),
                remarks
        );
        return departmentReviewRepository.findById(id).orElse(review);
    }

    private void validateOfficerAccess(DepartmentReview review, User officer) {
        if (officer.getRole().getName() == UserRole.ROLE_ADMIN || officer.getRole().getName() == UserRole.ROLE_SUPER_ADMIN) {
            return;
        }
        if (officer.getDepartmentCode() == null || !officer.getDepartmentCode().equalsIgnoreCase(review.getDepartmentCode().name())) {
            throw new UnauthorizedException("You are not authorized to act on " + review.getDepartmentCode().name() + " department reviews");
        }
    }
}
