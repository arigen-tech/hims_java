package com.hims.entity.repository;

import com.hims.entity.AppSetup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppSetupRepository extends JpaRepository<AppSetup, Long> {
    List<AppSetup> findByDepartmentIdAndDoctorIdAndSessionId(Long deptId,Long doctorId,Long sessionId);


}
