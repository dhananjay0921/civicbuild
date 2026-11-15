package com.civicbuild.repository;

import com.civicbuild.entity.DepartmentReview;
import com.civicbuild.enums.DepartmentCode;
import com.civicbuild.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentReviewRepository extends JpaRepository<DepartmentReview, Long> {
    List<DepartmentReview> findByApplicationId(Long applicationId);
    List<DepartmentReview> findByDepartmentCode(DepartmentCode departmentCode);
    List<DepartmentReview> findByDepartmentCodeAndStatus(DepartmentCode departmentCode, ReviewStatus status);
    Optional<DepartmentReview> findByApplicationIdAndDepartmentCode(Long applicationId, DepartmentCode departmentCode);
    Long countByDepartmentCodeAndStatus(DepartmentCode departmentCode, ReviewStatus status);
}
