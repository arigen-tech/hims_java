package com.hims.entity.repository;

import com.hims.entity.IpdBillingHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IpdBillingHeaderRepository extends JpaRepository<IpdBillingHeader,Long> {

    Optional<IpdBillingHeader> findByInpatientId_InpatientId(Long inpatientId);
}
