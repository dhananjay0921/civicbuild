package com.civicbuild.repository;

import com.civicbuild.entity.Permit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PermitRepository extends JpaRepository<Permit, Long> {
    Optional<Permit> findByPermitNumber(String permitNumber);
    Optional<Permit> findByApplicationId(Long applicationId);
}
