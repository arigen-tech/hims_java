package com.hims.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasIpdTransferReasonRepository extends JpaRepository<MasIpdTransferReason,Long> {
    List<MasIpdTransferReason> findByStatusIgnoreCaseOrderByTransferReasonNameAsc(String lowerCase);

    List<MasIpdTransferReason> findAllByOrderByStatusDescLastUpdateDateDesc();
}
