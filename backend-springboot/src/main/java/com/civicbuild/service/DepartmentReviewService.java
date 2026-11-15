package com.civicbuild.service;

import com.civicbuild.entity.DepartmentReview;
import com.civicbuild.entity.User;
import java.util.List;

public interface DepartmentReviewService {
    List<DepartmentReview> getAssignedReviews(User officer);
    DepartmentReview getReviewById(Long id, User officer);
    DepartmentReview approveReview(Long id, String remarks, User officer);
    DepartmentReview rejectReview(Long id, String remarks, User officer);
    DepartmentReview requestRevision(Long id, String remarks, User officer);
}
