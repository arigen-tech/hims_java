package com.hims.entity.repository;

import com.hims.entity.DgOrderDt;
import com.hims.entity.RadOrderDt;
import com.hims.entity.RadOrderHd;
import com.hims.projection.RadiologyBillingProjection;
import com.hims.projection.RadiologyProjection;
import io.netty.handler.codec.http2.Http2Connection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface RadOrderDtRepository extends JpaRepository<RadOrderDt, Long> {
    @Modifying
    @Transactional
    @Query("""
            UPDATE RadOrderDt b 
            SET b.billingStatus = :billingStatus
            WHERE b.packageId.packId = :pkgId
            AND b.billingHd.id = :billHdId
            """)
    void updatePaymentStatusPackegDt(
            @Param("billingStatus") String billingStatus,
            @Param("pkgId") Long pkgId,
            @Param("billHdId") Long billHdId
    );

    @Query("""
            select b from RadOrderDt b
            WHERE b.billingHd.id = :billHdId
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
  and dt.subChargecode.subId  = :modalityId
 and (
        :patientName is null
        or lower(
              function('replace',
                concat(
                  concat(coalesce(p.patientFn,''), coalesce(p.patientMn,'')),
                  coalesce(p.patientLn,'')
                ),
                ' ', ''
              )
           ) like :patientName
      )
and (
        :phoneNumber is null
        or function('replace', coalesce(p.patientMobileNumber,''), ' ', '')
           like :phoneNumber
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
    @Query("""
select
  dt.id as radOrderdtId,
   dt.reportStatus as reportStatus,
   dt.studyStatus as studyStatus,
  dt.orderAccessionNo as orderAccessionNo,
   p.uhidNo as uhid,
  concat(coalesce(p.patientFn,''),' ',coalesce(p.patientMn,''),' ',coalesce(p.patientLn,'')) as patientName,
  p.patientAge as age,
   p.patientGender.genderName as gender,
  p.patientMobileNumber as mobileNo,
  sc.subId as modalityId,
  sc.subName as modalityName,
  inv.investigationId as investigationId,
  inv.investigationName as investigationName,
  hd.orderTime as orderTime,
  hd.orderDate as orderDate,
  hd.department.departmentName as department
from RadOrderDt dt
join dt.radOrderhd hd
left join hd.patient p
join dt.subChargecode sc
join dt.investigation inv
where hd.hospital.id = :hospitalId
  and lower(dt.studyStatus) = lower(:studyStatus)
  and lower(dt.reportStatus) in (:reportStatuses)
  and (:modalityId is null or sc.subId = :modalityId)
  and (
        :patientName is null
        or lower(
              function('replace',
                concat(
                  concat(coalesce(p.patientFn,''), coalesce(p.patientMn,'')),
                  coalesce(p.patientLn,'')
                ),
                ' ', ''
              )
           ) like :patientName
      )
  and (
        :phoneNumber is null
        or function('replace', coalesce(p.patientMobileNumber,''), ' ', '')
           like :phoneNumber
  )
""")
    Page<RadiologyProjection> getPendingReportRadiologyProjection(
            @Param("hospitalId") Long hospitalId,
            @Param("studyStatus") String studyStatus,
            @Param("reportStatuses") List<String> reportStatuses,
            @Param("modalityId") Long modalityId,
            @Param("patientName") String patientName,
            @Param("phoneNumber") String phoneNumber,
            Pageable pageable
    );
    @Query("""
select
  dt.id as radOrderdtId,
  dt.reportStatus as reportStatus,
  dt.studyStatus as studyStatus,
  dt.orderAccessionNo as orderAccessionNo,
  p.uhidNo as uhid,
  concat(coalesce(p.patientFn,''),' ',coalesce(p.patientMn,''),' ',coalesce(p.patientLn,'')) as patientName,
  p.patientAge as age,
  p.patientGender.genderName as gender,
  p.patientMobileNumber as mobileNo,
  sc.subId as modalityId,
  sc.subName as modalityName,
 inv.investigationId as investigationId,
 inv.investigationName as investigationName,
 hd.orderTime as orderTime,
 hd.orderDate as orderDate,
 hd.department.departmentName as department,
  pacs.studyDatetime as studyDatetime

from RadOrderDt dt
join dt.radOrderhd hd
left join hd.patient p
join dt.subChargecode sc
join dt.investigation inv
left join PacsHmisStudy pacs
       on pacs.orderNo = dt.orderAccessionNo
      and pacs.uhid = p.uhidNo
where hd.hospital.id = :hospitalId
  and lower(dt.studyStatus) = lower(:studyStatus)
  and (:modalityId is null or sc.subId = :modalityId)
  and (
        :patientName is null
        or lower(
              function('replace',
                concat(
                  concat(coalesce(p.patientFn,''), coalesce(p.patientMn,'')),
                  coalesce(p.patientLn,'')
                ),
                ' ', ''
              )
           ) like :patientName
      )
  and (
        :phoneNumber is null
        or function('replace', coalesce(p.patientMobileNumber,''), ' ', '')
           like :phoneNumber
  )
""")
    Page<RadiologyProjection> getRadiologyPACSStudyList(
            @Param("hospitalId") Long hospitalId,
            @Param("studyStatus") String studyStatus,
            @Param("modalityId") Long modalityId,
            @Param("patientName") String patientName,
            @Param("phoneNumber") String phoneNumber,
            Pageable pageable
    );

    @Query(value = """
SELECT
    i.inpatient_id AS inpatientId, i.admission_no AS admissionNo, i.admission_date AS admissionDate,
    concat(coalesce(p.p_fn,''), coalesce(p.p_ln,'')) as patientName,
    p.uhid_no AS uhid, p.p_age AS age, p.p_mobile_number AS mobileNo, rd.rad_orderhd_id AS radOrderhdId,
    rd.rad_orderdt_id AS radOrderdtId, rd.order_accession_no AS orderAccessionNo, rh.order_date AS orderDate,
    rh.order_time AS orderTime, dmi.investigation_id AS investigationId, dmi.investigation_name AS investigationName,
    phs.study_datetime AS studyDatetime, rd.study_status AS studyStatus, rd.report_status AS reportStatus,
    rd.remarks AS remarks, rd.pacs_completion_status AS pacsCompletionStatus, msc.sub_chargecode_id AS modalityId,
    msc.sub_chargecode_name AS modalityName, md.department_name AS department

FROM rad_orderdt rd

INNER JOIN rad_orderhd rh
    ON rd.rad_orderhd_id = rh.rad_orderhd_id
LEFT JOIN inpatient i
    ON rh.inpatient_id = i.inpatient_id
LEFT JOIN patient p
    ON i.patient = p.patient_id
LEFT JOIN pacs_hmis_study phs
    ON rd.order_accession_no = phs.order_no
    AND phs.uhid = p.uhid_no
LEFT JOIN mas_sub_chargecode msc
    ON rd.sub_chargecode_id = msc.sub_chargecode_id
LEFT JOIN dg_mas_investigation dmi
    ON rd.investigation_id = dmi.investigation_id
LEFT JOIN mas_department md
    ON rh.department_id = md.department_id 
where 
(:inpatientId is null or i.inpatient_id = :inpatientId) and
(:accesionNo is null or rd.order_accession_no = :accesionNo)
""", nativeQuery = true)
    List<Map<String, Object>> orderTrackingByInpatientIdOrAccesionNo(@Param("inpatientId") Long inpatientId, @Param("accesionNo") String accesionNo);

    List<RadOrderDt> findByRadOrderhd(RadOrderHd hdObj);
}
