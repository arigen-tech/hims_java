package com.hims.entity.repository;

import com.hims.entity.BillingHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingHeaderRepository extends JpaRepository<BillingHeader, Integer> {

    List<BillingHeader> findByPaymentStatusIn(List<String> paymentStatuses);

}
