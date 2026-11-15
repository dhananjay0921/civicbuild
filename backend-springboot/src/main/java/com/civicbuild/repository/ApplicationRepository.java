package com.civicbuild.repository;

import com.civicbuild.entity.Application;
import com.civicbuild.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByApplicationNumber(String applicationNumber);
    Optional<Application> findByProcessInstanceKey(String processInstanceKey);
    List<Application> findByApplicantId(Long applicantId);
    List<Application> findByStatus(ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = :status")
    Long countByStatus(ApplicationStatus status);
}
