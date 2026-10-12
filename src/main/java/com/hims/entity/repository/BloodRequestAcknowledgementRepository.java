package com.hims.entity.repository;

import com.hims.entity.BloodRequestAcknowledgement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BloodRequestAcknowledgementRepository
        extends JpaRepository<BloodRequestAcknowledgement, Long> {

    boolean existsByAllocation_AllocationId(Long allocationId);

    Optional<BloodRequestAcknowledgement> findByAllocation_AllocationId(Long allocationId);
    
}
