package com.hims.entity.repository;

import com.hims.entity.LabOrderTrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabOrderTrackingStatusRepository extends JpaRepository<LabOrderTrackingStatus,Long> {
}
