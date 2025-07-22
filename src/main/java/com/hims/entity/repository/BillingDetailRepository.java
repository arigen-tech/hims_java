package com.hims.entity.repository;

import com.hims.entity.BillingDetail;
import com.hims.entity.BillingHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;

public interface BillingDetailRepository extends JpaRepository<BillingDetail, Integer> {

    List<BillingDetail> findByBillingHd(BillingHeader objHeader);

    @Modifying
    @Query("UPDATE BillingDetail b SET b.paymentStatus = :payment_status WHERE b.investigation.id = :investigationId AND b.billingHd.id = :billHdId")
    void updatePaymentStatusInvestigation(@Param("payment_status") String payment_status,
                                          @Param("investigationId") int investigationId,
                                          @Param("billHdId") int billHdId);


    @Modifying
    @Query("UPDATE BillingDetail b SET b.paymentStatus = :payment_status WHERE b.packageField.id = :pkgId AND b.billingHd.id = :billHdId")
    void updatePaymentStatuPackeg(@Param("payment_status") String payment_status,
                                    @Param("pkgId") int pkgId,
                                    @Param("billHdId") int billHdId);


    List<BillingDetail> findByBillHdIdAndPaymentStatusIn(Long id, List<String> n);


    List<BillingDetail> findByBillHd_Id(Long billHdId);

}
