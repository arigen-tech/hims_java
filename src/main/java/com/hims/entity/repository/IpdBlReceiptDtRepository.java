package com.hims.entity.repository;

import com.hims.entity.IpdBlReceiptDt;
import com.hims.response.PreviousPaymentHistoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpdBlReceiptDtRepository extends JpaRepository<IpdBlReceiptDt,Long> {
    @Query("""
            SELECT new com.hims.response.PreviousPaymentHistoryResponse(
                rh.receiptId,
                rh.receiptDate,
                rt.id,
                rt.receiptTypeName,
                pm.paymentModeId,
                pm.modeName,
                rd.amount
            )
            FROM IpdBlReceiptDt rd
            JOIN rd.receipt rh
            JOIN rh.receiptType rt
            JOIN rd.paymentMode pm
            WHERE rh.bill.billId = :billingHeaderId
            ORDER BY rh.receiptDate DESC
            """)
    List<PreviousPaymentHistoryResponse> previousPaymentHistory(
            @Param("billingHeaderId") Long billingHeaderId);
}