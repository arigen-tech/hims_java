package com.hims.entity.repository;

import com.hims.entity.MasAppointmentChangeReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MasAppointmentChangeReasonRepository extends JpaRepository<MasAppointmentChangeReason, Long> {

    List<MasAppointmentChangeReason> findAllByOrderByStatusDescLastUpdateDateDesc();

    List<MasAppointmentChangeReason> findByStatusIgnoreCaseOrderByReasonNameAsc(String y);
}
