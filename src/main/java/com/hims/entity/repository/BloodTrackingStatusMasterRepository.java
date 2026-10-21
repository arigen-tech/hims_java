package com.hims.entity.repository;

import com.hims.entity.BloodTrackingStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodTrackingStatusMasterRepository
        extends JpaRepository<BloodTrackingStatusMaster, Long> {
    List<BloodTrackingStatusMaster> findByStatusCodeInIgnoreCase(List<String> statusCode);
}