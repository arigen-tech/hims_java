package com.hims.entity.repository;

import com.hims.entity.MasOtScheduleChangeReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasOtScheduleChangeReasonRepository extends JpaRepository<MasOtScheduleChangeReason, Long> {

    List<MasOtScheduleChangeReason> findByStatusIgnoreCaseOrderByApplicableForAscReasonAsc(String status);

    List<MasOtScheduleChangeReason> findAllByOrderByStatusDescLastChgDateDesc();
}
