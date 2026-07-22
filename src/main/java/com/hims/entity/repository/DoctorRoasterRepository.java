package com.hims.entity.repository;

import com.hims.entity.*;
import com.hims.projection.DoctorRosterWeeklyProjection;
import com.hims.projection.GetDoctorRosterProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.*;

public interface DoctorRoasterRepository extends JpaRepository<DoctorRoaster, Integer> {

    @Query("SELECT dr FROM DoctorRoaster dr WHERE dr.department.id = :deptId AND dr.roasterDate = :rosterDate")
    List<DoctorRoaster> findDoctorRosterByDept(@Param("deptId") Long deptId,
                                               @Param("rosterDate") Date rosterDate);

    @Query("SELECT dr FROM DoctorRoaster dr WHERE dr.department.id = :deptId AND dr.doctorId.userId = :doctorId AND dr.roasterDate = :rosterDate")
    List<DoctorRoaster> findDoctorRosterByDeptAndDoctor(@Param("deptId") Long deptId,
                                                        @Param("doctorId") Long doctorId,
                                                        @Param("rosterDate") Date rosterDate);



    @Query(value = "SELECT * FROM doctor_roaster dr WHERE dr.department_id = :deptId AND DATE(dr.roaster_date) >= DATE(:rosterDate) AND DATE(dr.roaster_date) < DATE(:endDate) ORDER BY dr.roaster_date", nativeQuery = true)
    List<DoctorRoaster> findDoctorRostersByDept(
            @Param("deptId") Long deptId,
            @Param("rosterDate") Date rosterDate,
            @Param("endDate") Date endDate
    );
    @Query("SELECT dr FROM DoctorRoaster dr WHERE dr.department.id = :deptId AND dr.doctorId.userId = :doctorId AND dr.roasterDate >= :rosterDate AND dr.roasterDate < :endDate ORDER BY dr.roasterDate")
    List<DoctorRoaster> findDoctorRostersByDeptAndDoctor(
            @Param("deptId") Long deptId,
            @Param("doctorId") Long doctorId,
            @Param("rosterDate") Date rosterDate,
            @Param("endDate") Date endDate
    );


    @Query("""
    select 
        dr.id as id,
        dr.roasterDate as roasterDate,
        dr.roasterValue as roasterValue,
        d.userId as doctorUserId
    from DoctorRoaster dr
    join dr.doctorId d
    where dr.department.id = :deptId
      and d.userId = :doctorId
      and dr.roasterDate >= :startDate
      and dr.roasterDate < :endDate
    order by dr.roasterDate
""")
    List<DoctorRosterWeeklyProjection> findWeeklyByDeptAndDoctor(
            @Param("deptId") Long deptId,
            @Param("doctorId") Long doctorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    @Query("""
    select 
        dr.id as id,
        dr.roasterDate as roasterDate,
        dr.roasterValue as roasterValue,
        d.userId as doctorUserId
    from DoctorRoaster dr
    left join dr.doctorId d
    where dr.department.id = :deptId
      and dr.roasterDate >= :startDate
      and dr.roasterDate < :endDate
    order by dr.roasterDate
""")
    List<DoctorRosterWeeklyProjection> findWeeklyByDept(
            @Param("deptId") Long deptId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    List<DoctorRoaster> findByIdIn(Collection<Long> ids);

    @Query("""
        SELECT
          dr.id                 AS id,
          dr.hospital.id        AS hospitalId,
          dr.department.id      AS deptmentId,
          dr.chgBy              AS chgBy,
          dr.chgDate            AS chgDate,
          dr.chgTime            AS chgTime,
          dr.doctorId.userId    AS doctorId,
          dr.roasterValue       AS rosterVal,
          dr.roasterDate        AS roasterDate
        FROM DoctorRoaster dr
        WHERE dr.department.id = :deptId
          AND dr.doctorId.userId = :doctorId
          AND dr.roasterDate = :rosterDate
    """)
    List<GetDoctorRosterProjection> findDoctorRosterViewByDeptAndDoctor(@Param("deptId") Long deptId,
                                                               @Param("doctorId") Long doctorId,
                                                               @Param("rosterDate") Date rosterDate);

    @Query("""
        SELECT
          dr.id                 AS id,
          dr.hospital.id        AS hospitalId,
          dr.department.id      AS deptmentId,
          dr.chgBy              AS chgBy,
          dr.chgDate            AS chgDate,
          dr.chgTime            AS chgTime,
          dr.doctorId.userId    AS doctorId,
          dr.roasterValue       AS rosterVal,
          dr.roasterDate        AS roasterDate
        FROM DoctorRoaster dr
        WHERE dr.department.id = :deptId
          AND dr.roasterDate = :rosterDate
    """)
    List<GetDoctorRosterProjection> findDoctorRosterViewByDept(@Param("deptId") Long deptId,
                                                      @Param("rosterDate") Date rosterDate);

}
