package com.hims.entity.repository;

import com.hims.entity.BillingHeader;
import com.hims.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BillingHeaderRepository extends JpaRepository<BillingHeader, Integer> {
    List<BillingHeader> findByPaymentStatusIn(List<String> paymentStatuses);
    BillingHeader findByBillNoAndPaymentStatus(String billNo, String paymentStatus);
    BillingHeader findByVisit(Visit visit);
}
