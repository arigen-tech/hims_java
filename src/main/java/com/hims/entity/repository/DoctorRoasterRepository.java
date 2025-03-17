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

    @Query("SELECT dr FROM DoctorRoaster dr WHERE dr.department.id = :deptId AND dr.doctorId.userId = :doctorId AND dr.roasterDate = :rosterDate")
    List<DoctorRoaster> findDoctorRosterByIds(@Param("deptId") Long deptId,
                                             @Param("doctorId") Long doctorId,
                                             @Param("rosterDate") LocalDate rosterDate);
}


