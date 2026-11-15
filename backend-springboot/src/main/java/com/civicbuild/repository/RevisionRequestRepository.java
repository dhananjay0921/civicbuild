package com.civicbuild.repository;

import com.civicbuild.entity.RevisionRequest;
import com.civicbuild.enums.DepartmentCode;
import com.civicbuild.enums.RevisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RevisionRequestRepository extends JpaRepository<RevisionRequest, Long> {
    List<RevisionRequest> findByApplicationId(Long applicationId);
    List<RevisionRequest> findByApplicationIdAndStatus(Long applicationId, RevisionStatus status);
    List<RevisionRequest> findByApplicationIdAndDepartmentCode(Long applicationId, DepartmentCode departmentCode);
}
