package com.hims.entity.repository;

import com.hims.entity.MasSubChargeCode;
import com.hims.projection.ModalityDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasSubChargeCodeRepository extends JpaRepository<MasSubChargeCode, Long> {
    List<MasSubChargeCode> findByStatus(String status);

    List<MasSubChargeCode> findByStatusOrderBySubNameAsc(String y);

 //   List<MasSubChargeCode> findAllByOrderByLastChgDateDescLastChgTimeDesc();

    List<MasSubChargeCode> findAllByOrderByStatusDescLastChgDateDescLastChgTimeDesc();



    @Query("""
    select s.subId as id,
           s.subName as modalityName
    from MasSubChargeCode s
    where lower(s.masDepartment.departmentCode) = lower(:deptCode)
      and lower(s.status) = lower(:status)
    order by s.subCode
""")
    List<ModalityDetailsProjection> findModalityByDepartmentCode(@Param("deptCode") String deptCode,
                                                                 @Param("status") String status);
}
