package com.hims.entity.repository;

import com.hims.entity.DoctorRoaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface DoctorRoasterRepository extends JpaRepository<DoctorRoaster, Integer> {

//    @Query("SELECT a FROM Appointment a WHERE a.department.id = :departmentId AND a.doctor.id = :doctorId AND a.fromDate = :fromDate")
//    List<DoctorRoaster> findByDepartmentDoctorAndDate(
//            @Param("departmentId") Long departmentId,
//            @Param("doctorId") Long doctorId,
//            @Param("fromDate") Date fromDate);
}
