package com.hims.entity.repository;

import com.hims.entity.*;
import com.hims.projection.MasServiceOpdProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MasServiceOpdRepository extends JpaRepository<MasServiceOpd, Long> {
    List<MasServiceOpd> findByHospitalIdId(Long hospitalId);


    @Query("SELECT a FROM MasServiceOpd a " +
            "WHERE a.hospitalId = :hospital " +
            "AND a.doctorId = :doctor " +
            "AND a.departmentId = :department " +
            "AND a.serviceCategory = :serviceCat " +
            "AND :currentDateTime BETWEEN a.fromDt AND a.toDt " +
            "AND LOWER(a.status) = 'y'")
    Optional<MasServiceOpd> findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatIdAndCurrentDate(
            @Param("hospital") MasHospital hospital,
            @Param("doctor") User doctor,
            @Param("department") MasDepartment department,
            @Param("serviceCat") MasServiceCategory serviceCat,
            @Param("currentDateTime") Instant currentDateTime);


    @Query("""
        SELECT s
        FROM MasServiceOpd s
        WHERE s.doctorId.userId IN :userIds
        """)
    List<MasServiceOpd> getOPDServiceByUserIds(
        @Param("userIds") List<Long> userIds
    );

    @Query("""
        SELECT a.baseTariff
        FROM MasServiceOpd a
        WHERE a.hospitalId.id = :hospitalId
          AND a.doctorId.userId = :doctorId
          AND a.departmentId.id = :deptId
          AND a.serviceCategory = :serviceCat
          AND :currentDateTime BETWEEN a.fromDt AND a.toDt
          AND a.status = 'y'
    """)
    Optional<BigDecimal> findBaseTariffForDoctor(@Param("hospitalId") Long hospitalId,
                                                 @Param("doctorId") Long doctorId,
                                                 @Param("deptId") Long deptId,
                                                 @Param("serviceCat") MasServiceCategory serviceCat,
                                                 @Param("currentDateTime") Instant currentDateTime);


    @Query(value = """
    SELECT 
        mso.id AS id,
        mso.service_name AS serviceName,
        mso.base_tariff AS baseTariff,
        msc.service_cat_name AS serviceCategory,
        ms.department_name AS departmentName,
        me.emp_fn AS doctorFirstName,
        me.emp_mn AS doctorMiddleName,
        me.emp_ln AS doctorLastName,
        mso.from_dt AS fromDate,
        mso.to_dt AS toDate,
        mso.status AS status
    FROM mas_service_opd mso
    LEFT JOIN mas_service_category msc 
        ON mso.service_cat_id = msc.id
    LEFT JOIN mas_department ms  
        ON mso.department_id = ms.department_id
    LEFT JOIN users u  
        ON u.user_id = mso.doctor_id
    LEFT JOIN mas_employee me  
        ON u.employee_id = me.emp_id 
    WHERE mso.hospital_id = :hospitalId
    AND (:departmentId IS NULL OR mso.department_id = :departmentId)
    AND (:doctorId IS NULL OR mso.doctor_id = :doctorId)
    AND (
        :doctorName IS NULL OR
        LOWER(CONCAT(
            COALESCE(me.emp_fn,''), ' ',
            COALESCE(me.emp_mn,''), ' ',
            COALESCE(me.emp_ln,'')
        )) LIKE LOWER(CONCAT('%', :doctorName, '%'))
    )

    ORDER BY ms.department_name ASC
""",
            countQuery = """
    SELECT COUNT(*) 
    FROM mas_service_opd mso
    LEFT JOIN users u ON u.user_id = mso.doctor_id
    LEFT JOIN mas_employee me ON u.employee_id = me.emp_id
    WHERE mso.hospital_id = :hospitalId
    AND (:departmentId IS NULL OR mso.department_id = :departmentId)
    AND (:doctorId IS NULL OR mso.doctor_id = :doctorId)
    
    AND (
        :doctorName IS NULL OR
        LOWER(CONCAT(
            COALESCE(me.emp_fn,''), ' ',
            COALESCE(me.emp_mn,''), ' ',
            COALESCE(me.emp_ln,'')
        )) LIKE LOWER(CONCAT('%', :doctorName, '%'))
    )
""",
            nativeQuery = true)
    Page<MasServiceOpdProjection> getOpdTariffByDepartmentAndDoctor(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("doctorId") Long doctorId,
            @Param("doctorName") String doctorName,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(o) > 0
        FROM MasServiceOpd o
        WHERE o.serviceCategory.id = :serviceCategoryId
          AND o.hospitalId.id = :hospitalId
          AND o.departmentId.id = :departmentId
          AND o.doctorId.userId = :doctorId
          AND o.fromDt <= :toDate
          AND o.toDt >= :fromDate
          AND LOWER(o.status) = :status
    """)
    boolean existsOverlappingTariff(
            @Param("serviceCategoryId") Long serviceCategoryId,
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("doctorId") Long doctorId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            @Param("status") String status
    );

}

