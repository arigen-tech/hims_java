package com.hims.entity.repository;

import com.hims.entity.BillingHeader;
import com.hims.entity.DgMasInvestigation;
import com.hims.entity.DgMasInvestigation;
import com.hims.entity.DgOrderDt;
import com.hims.entity.DgOrderHd;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Optional;
import java.util.Set;

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

    List<DgOrderDt> findByOrderhdIdId(int orderHdId);

    List<DgOrderDt> findByOrderhdIdAndBillingStatus(DgOrderHd orderHd, String n);

    Optional<DgOrderDt> findByOrderhdIdAndInvestigationId(DgOrderHd existingOrderHd, DgMasInvestigation invEntity);

    @Modifying
    @Query("UPDATE DgOrderDt d SET d.orderStatus = :status WHERE d.id = :id")
    void updateOrderStatus(Long id, String status);


    @Query("SELECT d.orderhdId.id FROM DgOrderDt d WHERE d.id IN :detailIds")
    Set<Long> findOrderHdIdsByDetailIds(@Param("detailIds") List<Long> detailIds);

    @Query("SELECT COUNT(d) FROM DgOrderDt d WHERE d.orderhdId.id = :hdId")
    long countTotalByOrderHd(@Param("hdId") Long hdId);


    @Query("SELECT COUNT(d) FROM DgOrderDt d WHERE d.orderhdId.id = :hdId AND d.orderStatus = 'y'")
    long countAcceptedByOrderHd(@Param("hdId") Long hdId);


    @Query("SELECT d FROM DgOrderDt d WHERE d.orderhdId.id = :orderHdId AND d.investigationId.investigationId = :invId")
    DgOrderDt findByOrderHdIdAndInvestigationId(Long orderHdId, Long invId);

    @Query("SELECT d.orderStatus FROM DgOrderDt d WHERE d.orderhdId.id = :orderHdId")
    List<String> getOrderStatusesOfOrderHd(Long orderHdId);



    DgOrderDt findByOrderhdId_IdAndInvestigationId_InvestigationId(long id, Long investigationId);



//SELECT b FROM DgOrderDt b WHERE b.billingHd.id = :billHdId AND b.billingStatus = 'y'

}
