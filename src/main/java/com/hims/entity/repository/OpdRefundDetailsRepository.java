package com.hims.entity.repository;

import com.hims.entity.OpdRefundDetails;
import com.hims.projection.PatientBillingRefundDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpdRefundDetailsRepository
        extends JpaRepository<OpdRefundDetails, Long> {

    @Query(value = """
            SELECT
                CASE WHEN ord.processed_date IS NOT NULL THEN 'COMPLETED' ELSE 'PENDING' END AS refundStatus,
                COALESCE(ord.refund_amout, 0) AS refundAmount,
                ord.refund_mode AS refundMode,
                ord.txn_no AS transactionNumber,
                ord.refund_date AS refundDate,
                ord.processed_by AS processedBy
            FROM opd_refund_details ord
            WHERE ord.billing_hd_id = :billingId
            ORDER BY ord.refund_date DESC
            """, nativeQuery = true)
    List<PatientBillingRefundDetailsProjection> findRefundDetailsByBillingId(@Param("billingId") Long billingId
    );
}
