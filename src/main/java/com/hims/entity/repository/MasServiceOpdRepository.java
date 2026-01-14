package com.hims.entity.repository;

import com.hims.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MasServiceOpdRepository extends JpaRepository<MasServiceOpd, Long> {
    List<MasServiceOpd> findByHospitalIdId(Long hospitalId);

//    Optional<MasServiceOpd> findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatId(MasHospital hospital, User doctor, MasDepartment department, MasServiceCategory serviceCategory);

//    @Query("SELECT a FROM MasServiceOpd a WHERE a.hospitalId = :hospital AND a.doctorId = :doctor AND a.departmentId = :department AND a.serviceCategory = :serviceCat")
//    Optional<MasServiceOpd> findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatId(@Param("hospital") MasHospital hospital,
//                                      @Param("doctor") User doctor,
//                                      @Param("department") MasDepartment department,
//                                      @Param("serviceCat") MasServiceCategory serviceCat);

    @Query("SELECT a FROM MasServiceOpd a " +
            "WHERE a.hospitalId = :hospital " +
            "AND a.doctorId = :doctor " +
            "AND a.departmentId = :department " +
            "AND a.serviceCategory = :serviceCat " +
            "AND :currentDateTime BETWEEN a.fromDt AND a.toDt " +
            "AND a.status = 'y'")
    Optional<MasServiceOpd> findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatIdAndCurrentDate(
            @Param("hospital") MasHospital hospital,
            @Param("doctor") User doctor,
            @Param("department") MasDepartment department,
            @Param("serviceCat") MasServiceCategory serviceCat,
            @Param("currentDateTime") Instant currentDateTime);




    Optional<MasServiceOpd> findByDoctorId_UserId(Long userId);
}
