package com.hims.entity.repository;

import com.hims.entity.IpdConsultationTariff;
import com.hims.projection.IpdConsultationTariffProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpdConsultationTariffRepository extends JpaRepository<IpdConsultationTariff,Long> {



        @Query("""
        SELECT 
            t.tariffId AS tariffId,
            sc.id AS serviceCategoryId,
            sc.serviceCatName AS serviceCategoryName,
            vt.visitTypeId AS visitTypeId,
            vt.visitTypeName AS visitTypeName,
            d.id AS departmentId,
            d.departmentName AS departmentName,
            u.id AS doctorId,
            CONCAT(em.firstName, ' ', em.lastName) AS doctorName,
            t.baseTariff AS baseTariff,
            t.fromDate AS fromDate,
            t.toDate AS toDate,
            t.status AS status
        FROM IpdConsultationTariff t
        LEFT JOIN t.serviceCategory sc
        LEFT JOIN t.visitType vt
        LEFT JOIN t.department d
        LEFT JOIN t.doctor u
        LEFT JOIN u.employee em
        WHERE (:departmentId IS NULL OR d.id = :departmentId)
          AND (:doctorId IS NULL OR u.id = :doctorId)
          ORDER BY t.lastChangedDate DESC
    """)
        Page<IpdConsultationTariffProjection> getAllIpdConsultationTariff(@Param("departmentId") Long departmentId,
                @Param("doctorId") Long doctorId,
                Pageable pageable
        );


}

