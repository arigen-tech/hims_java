package com.hims.entity.repository;

import com.hims.entity.MasComponentFailureReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasComponentFailureReasonRepository extends JpaRepository<MasComponentFailureReason,Long> {
    List<MasComponentFailureReason> findByStatusIgnoreCaseOrderByFailureReasonNameAsc(String y);

    List<MasComponentFailureReason> findAllByOrderByLastUpdateDateDesc();
}
