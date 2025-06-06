package com.hims.entity.repository;

import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppSetupRepository extends JpaRepository<AppSetup, Long> {
//    List<AppSetup> findByDeptAndDoctorIdAndSessionId(Long deptId,Long doctorId,Long sessionId);
    List<AppSetup> findByDeptAndDoctorIdAndSessionId(MasDepartment dept, User doctorId, MasOpdSession sessionId);

    @Query("SELECT a FROM AppSetup a WHERE a.dept.id = :deptId AND a.doctorId.userId = :doctorId AND a.session.id = :sessionId")
    List<AppSetup> findAppSetupsByIds(@Param("deptId") Long deptId,
                                      @Param("doctorId") Long doctorId,
                                      @Param("sessionId") Long sessionId);
    Optional<AppSetup> findByDeptAndDoctorIdAndSession(MasDepartment dept, User doctorId, MasOpdSession session);


    @Query("SELECT COUNT(a) FROM AppSetup a WHERE a.dept = :department AND a.doctorId = :doctor AND a.session = :session")
    long countByDeptAndDoctorIdAndSession(@Param("department") MasDepartment department,
                                          @Param("doctor") User doctor,
                                          @Param("session") MasOpdSession session);
    @Query("SELECT a FROM AppSetup a " +
            "WHERE a.doctorId.id = :doctorId " +
            "AND a.hospital.id = :hospitalId " +
            "AND a.session.id = :sessionId " +
            "AND a.days LIKE %:dayName%")
    Optional<AppSetup> findByDoctorHospitalSessionAndDayName(
            @Param("doctorId") Long doctorId,
            @Param("hospitalId") Long hospitalId,
            @Param("sessionId") Long sessionId,
            @Param("dayName") String dayName);


}
