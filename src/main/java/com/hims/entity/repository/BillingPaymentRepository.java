package com.hims.entity.repository;

import com.hims.entity.BillingPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingPaymentRepository extends JpaRepository<BillingPayment, Long> {
}
