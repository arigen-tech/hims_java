package com.hims.entity.repository;

import com.hims.entity.MasDischargeReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasDischargeReasonRepository extends JpaRepository<MasDischargeReason,Long> {

    @Query(value = "SELECT * FROM public.mas_discharge_reason WHERE lower(status) = lower(:status) ORDER BY reason_name ASC", nativeQuery = true)
    List<MasDischargeReason> findByStatusIgnoreCaseOrderByReasonNameAsc(@Param("status") String status);

    @Query(value = "SELECT * FROM public.mas_discharge_reason ORDER BY reason_name ASC", nativeQuery = true)
    List<MasDischargeReason> findAllByOrderByStatusDescLastUpdateDateDesc();
}
