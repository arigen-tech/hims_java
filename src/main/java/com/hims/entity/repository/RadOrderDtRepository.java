package com.hims.entity.repository;

import com.hims.entity.RadOrderDt;
import io.netty.handler.codec.http2.Http2Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RadOrderDtRepository extends JpaRepository<RadOrderDt, Long> {
//    @Query("""
//    select dt.orderAccessionNo,
//           p.uhidNo,
//           concat(coalesce(p.patientFn,''),' ',coalesce(p.patientMn,''),' ',coalesce(p.patientLn,'')),
//           p.patientAge,
//           g.genderName,
//           m.modalityName,
//           inv.investigationName,
//           hd.orderDate,
//           hd.orderTime,
//           dept.departmentName
//    from RadOrderDt dt
//    join dt.radOrderhd hd
//    join hd.patient p
//    left join p.patientGender g
//    left join hd.department dept
//    join dt.investigation inv
//    join inv.subChargecode m
//    where dt.billingStatus='y'
//      and (:patientId is null or p.id=:patientId)
//      and (:subChargecode is null or m.id=:modalityId)
//""")
//    List<Object[]> pendingRadiologyRaw(Long patientId, Long modalityId);

 //   List<RadOrderDt> findByBillingStatusIgnoreCase(String y);

    List<RadOrderDt> findByRadOrderhd_Hospital_IdAndBillingStatusIgnoreCase(Long id, String y);
}
