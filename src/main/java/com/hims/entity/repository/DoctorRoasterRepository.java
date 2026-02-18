package com.hims.entity.repository;

import com.hims.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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


}
