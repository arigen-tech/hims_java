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


    @Query("""
        SELECT bh FROM BillingHeader bh
        LEFT JOIN bh.visit v
        LEFT JOIN v.patient p
        WHERE bh.paymentStatus IN :statuses
        AND (:patientName IS NULL OR bh.patientDisplayName LIKE %:patientName%)
        AND (:uhidNo IS NULL OR p.uhidNo = :uhidNo)
    """)
    List<BillingHeader> searchPendingBilling(
            @Param("patientName") String patientName,
            @Param("uhidNo") String uhidNo,
            @Param("statuses") List<String> statuses
    );
}
