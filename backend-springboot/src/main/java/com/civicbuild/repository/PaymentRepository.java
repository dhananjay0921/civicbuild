package com.civicbuild.repository;

import com.civicbuild.entity.Payment;
import com.civicbuild.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByApplicationId(Long applicationId);
    Optional<Payment> findByTransactionRef(String transactionRef);
    Optional<Payment> findByApplicationIdAndStatus(Long applicationId, PaymentStatus status);
}
