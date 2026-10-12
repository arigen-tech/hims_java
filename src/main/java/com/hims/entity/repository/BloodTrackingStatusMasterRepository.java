package com.hims.entity.repository;

import com.hims.entity.BloodTrackingStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodTrackingStatusMasterRepository
        extends JpaRepository<BloodTrackingStatusMaster, Long> {
}