package com.hims.entity.repository;

import com.hims.entity.LabTurnAroundTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTurnAroundTimeRepository  extends JpaRepository<LabTurnAroundTime,Long> {
}
