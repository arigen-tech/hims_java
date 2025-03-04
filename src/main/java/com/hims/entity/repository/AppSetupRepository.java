package com.hims.entity.repository;

import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppSetupRepository extends JpaRepository<AppSetup, Long> {
//    List<AppSetup> findByDeptAndDoctorIdAndSessionId(Long deptId,Long doctorId,Long sessionId);
    List<AppSetup> findByDeptAndDoctorIdAndSessionId(MasDepartment dept, User doctorId, MasOpdSession sessionId);


}
