package com.hims.entity.repository;

import com.hims.entity.BillingHeader;
import com.hims.entity.DgOrderDt;
import com.hims.entity.DgOrderHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabDtRepository extends JpaRepository<DgOrderDt,Integer> {
 List<DgOrderDt> findByOrderhdId(DgOrderHd hdObj);

    @Modifying
    @Query("UPDATE DgOrderDt b SET b.billingStatus = :billing_status WHERE b.investigationId.id = :investigationId AND b.billingHd.id = :billHdId AND b.packageId IS NULL" )
    void updatePaymentStatusInvestigationDt(@Param("billing_status") String billing_status,
                                          @Param("investigationId") int investigationId,
                                          @Param("billHdId") int billHdId);


    @Modifying
    @Query("UPDATE DgOrderDt b SET b.billingStatus = :billing_status WHERE b.packageId.id = :pkgId AND b.billingHd.id = :billHdId")
    void updatePaymentStatusPackegDt(@Param("billing_status") String billing_status,
                                            @Param("pkgId") int pkgId,
                                            @Param("billHdId") int billHdId);

   @Modifying
   @Query("select b from DgOrderDt b  WHERE  b.billingHd.id = :billHdId AND  b.billingStatus = 'n'")
   List<DgOrderDt> findByStatus(@Param("billHdId") long billHdId);

    List<DgOrderDt> findByOrderhdIdAndBillingStatusAndOrderStatus(DgOrderHd orderhdId, String billingStatus, String orderStatus);
//SELECT b FROM DgOrderDt b WHERE b.billingHd.id = :billHdId AND b.billingStatus = 'y'

}
