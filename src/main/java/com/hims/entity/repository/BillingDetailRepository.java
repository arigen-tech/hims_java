package com.hims.entity.repository;

import com.hims.entity.BillingDetail;
import com.hims.entity.BillingHeader;
import com.hims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface BillingDetailRepository extends JpaRepository<BillingDetail, Integer> {

    List<BillingDetail> findByBillingHd(BillingHeader objHeader);

    @Transactional
    @Modifying
    @Query("""
    UPDATE BillingDetail b 
    SET b.paymentStatus = :paymentStatus,
        b.collectedBy = :collectedBy
    WHERE b.investigation.id = :investigationId 
    AND b.billingHd.id = :billHdId
""")
    void updatePaymentStatusInvestigation(
            @Param("paymentStatus") String paymentStatus,
            @Param("collectedBy") User collectedBy,
            @Param("investigationId") int investigationId,
            @Param("billHdId") int billHdId
    );
    @Modifying
    @Query("""
    UPDATE BillingDetail b 
    SET b.paymentStatus = :paymentStatus,
        b.collectedBy = :collectedBy
    WHERE b.packageField.id = :pkgId 
    AND b.billingHd.id = :billHdId
""")
    void updatePaymentStatusPackage(
            @Param("paymentStatus") String paymentStatus,
            @Param("collectedBy") User collectedBy,
            @Param("pkgId") int pkgId,
            @Param("billHdId") int billHdId
    );

    List<BillingDetail> findByBillHdIdAndPaymentStatusIn(Long id, List<String> n);


    List<BillingDetail> findByBillHdId(Long billHdId);

}
