package com.hims.entity.repository;

import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppSetupRepository extends JpaRepository<AppSetup, Long> {
//    List<AppSetup> findByDeptAndDoctorIdAndSessionId(Long deptId,Long doctorId,Long sessionId);
    List<AppSetup> findByDeptAndDoctorIdAndSessionId(MasDepartment dept, User doctorId, MasOpdSession sessionId);

    @Query("SELECT a FROM AppSetup a WHERE a.dept.id = :deptId AND a.doctorId.userId = :doctorId AND a.session.id = :sessionId")
    List<AppSetup> findAppSetupsByIds(@Param("deptId") Long deptId,
                                      @Param("doctorId") Long doctorId,
                                      @Param("sessionId") Long sessionId);

    // Or if you prefer using native SQL
    @Query(value = "SELECT * FROM public.app_setup WHERE dept_id = :deptId AND doctor_id = :doctorId AND session_id = :sessionId",
            nativeQuery = true)
    List<AppSetup> findAppSetupsByIdsNative(@Param("deptId") Long deptId,
                                            @Param("doctorId") Long doctorId,
                                            @Param("sessionId") Long sessionId);
}
