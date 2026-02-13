package com.hims.entity.repository;

import com.hims.entity.DgOrderDt;
import com.hims.entity.RadOrderDt;
import io.netty.handler.codec.http2.Http2Connection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RadOrderDtRepository extends JpaRepository<RadOrderDt, Long> {
    @Modifying
    @Transactional
    @Query("""
            UPDATE RadOrderDt b 
            SET b.billingStatus = :billingStatus
            WHERE b.packageId.packId = :pkgId
            AND b.billingHd.billingHdId = :billHdId
            """)
    void updatePaymentStatusPackegDt(
            @Param("billingStatus") String billingStatus,
            @Param("pkgId") Long pkgId,
            @Param("billHdId") Long billHdId
    );

    @Query("""
            select b from RadOrderDt b
            WHERE b.billingHd.billingHdId = :billHdId
            AND b.billingStatus = 'n'
            """)
    List<RadOrderDt> findUnbilledByBillingHdId(
            @Param("billHdId") Long billHdId
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE RadOrderDt b
            SET b.billingStatus = :billingStatus
            WHERE b.investigation.id = :investigationId AND b.billingHd.id = :billHdId
            """)
    void updatePaymentStatusInvestigationDt(
            @Param("billingStatus") String billingStatus,
            @Param("investigationId") int investigationId,
            @Param("billHdId") int billHdId
    );
    @Query("""
    select dt
    from RadOrderDt dt
    join dt.radOrderhd hd
    left join hd.patient p
    where hd.hospital.id = :hospitalId
      and lower(dt.billingStatus) = lower(:billingStatus)
      and lower(dt.studyStatus)   = lower(:studyStatus)
      and  dt.subChargecode.subId = :modalityId
      and (
             :patientName is null
              or lower(coalesce(p.patientFn,'')) like :patientName
              or lower(coalesce(p.patientMn,'')) like :patientName
              or lower(coalesce(p.patientLn,'')) like :patientName
              )
      and (
             :phoneNumber is null
              or p.patientMobileNumber like :phoneNumber
      )
""")
    Page<RadOrderDt> findPendingRadiology(
            @Param("hospitalId") Long hospitalId,
            @Param("billingStatus") String billingStatus,
            @Param("studyStatus") String studyStatus,
            @Param("modalityId") Long modalityId,
            @Param("patientName") String patientName,
            @Param("phoneNumber") String phoneNumber,
            Pageable pageable
    );
}