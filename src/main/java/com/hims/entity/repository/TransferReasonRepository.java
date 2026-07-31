package com.hims.entity.repository;

import com.hims.entity.TransferReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferReasonRepository extends JpaRepository<TransferReason, Long> {

    List<TransferReason> findByStatusIgnoreCaseOrderByTransferReasonNameAsc(String status);

    List<TransferReason> findAllByOrderByStatusDescLastUpdateDateDesc();
}