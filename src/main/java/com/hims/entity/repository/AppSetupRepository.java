package com.hims.entity.repository;

import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            "AND a.dept.Id = :departmentId " +
            "AND a.session.id = :sessionId " +
            "AND LOWER(a.days) = LOWER(:dayName)")
    List<AppSetup> findByDoctorHospitalSessionAndDayName(
            @Param("doctorId") Long doctorId,
            @Param("departmentId") Long departmentId,
            @Param("sessionId") Long sessionId,
            @Param("dayName") String dayName);

    List<AppSetup> findByDoctorId_UserId(Long userId);

    @Query(value = """
    WITH ranked_days AS (
        SELECT
            a.doctor_id,
            a.session_id,
            a.days,
            a.start_time,
            a.end_time,
            CASE a.days
                WHEN 'Sunday' THEN 0
                WHEN 'Monday' THEN 1
                WHEN 'Tuesday' THEN 2
                WHEN 'Wednesday' THEN 3
                WHEN 'Thursday' THEN 4
                WHEN 'Friday' THEN 5
                WHEN 'Saturday' THEN 6
            END AS day_number
        FROM app_setup a
        WHERE a.doctor_id = :doctorId
          AND a.start_time IS NOT NULL
          AND a.end_time IS NOT NULL
    )
    SELECT DISTINCT ON (r.session_id)
           r.doctor_id,
           r.session_id,
           r.days,
           r.start_time,
           r.end_time
    FROM ranked_days r
    ORDER BY 
        r.session_id,
        ((r.day_number - CAST(EXTRACT(DOW FROM CURRENT_DATE) AS integer) + 7) % 7),
        r.start_time
    """, nativeQuery = true)
    List<Object[]> findDistinctDoctorSessionNextDay(@Param("doctorId") Long doctorId);
    }



