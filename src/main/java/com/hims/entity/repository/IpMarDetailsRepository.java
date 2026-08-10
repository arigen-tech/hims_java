package com.hims.entity.repository;

import com.hims.entity.IpMarDetails;
import com.hims.projection.IpMarDetailsProjection;
import com.hims.projection.MarMedicineProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpMarDetailsRepository extends JpaRepository<IpMarDetails, Long> {

    @Query(value = """
            SELECT 
                imd.inpatient_id AS inpatientId, 
                imd.administration_time AS administrationTime, 
                msi.item_id AS itemId, 
                msi.nomenclature AS nomenclature, 
                mr.route_name AS routeName, 
                imp.dose AS dose, 
                imd.administered_qty AS administeredQty, 
                imd.batch_no AS batchNo, 
                imd.expiry_date AS expiryDate,
                imd.administered_by AS administeredBy, 
                imd.remarks AS remarks
            FROM ip_mar_details imd 
            LEFT JOIN ip_medicine_prescription imp ON imd.prescription_id = imp.prescription_id
            LEFT JOIN inpatient i ON imd.inpatient_id = i.inpatient_id
            LEFT JOIN mas_store_item msi ON imp.item_id = msi.item_id 
            LEFT JOIN mas_route mr ON imp.route_id = mr.route_id
            LEFT JOIN mas_frequency mf ON imp.frequency_id = mf.frequency_id
            WHERE imd.inpatient_id = :inpatientId 
              AND (:itemId IS NULL OR msi.item_id = :itemId)
            ORDER BY imd.administration_time DESC, msi.nomenclature ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM ip_mar_details imd 
            LEFT JOIN ip_medicine_prescription imp ON imd.prescription_id = imp.prescription_id
            LEFT JOIN mas_store_item msi ON imp.item_id = msi.item_id
            WHERE imd.inpatient_id = :inpatientId 
              AND (:itemId IS NULL OR msi.item_id = :itemId)
            """, nativeQuery = true)
    Page<IpMarDetailsProjection> getMarAdministrationLog(
            @Param("inpatientId") Long inpatientId,
            @Param("itemId") Long itemId,
            Pageable pageable
    );

    @Query(value = """
            SELECT DISTINCT
                msi.item_id AS itemId,
                msi.nomenclature AS nomenclature
            FROM ip_mar_details imd
            JOIN ip_medicine_prescription imp ON imd.prescription_id = imp.prescription_id
            JOIN mas_store_item msi ON imp.item_id = msi.item_id
            WHERE imd.inpatient_id = :inpatientId
            ORDER BY msi.nomenclature ASC
            """, nativeQuery = true)
    List<MarMedicineProjection> getUniqueMedicinesInMar(
            @Param("inpatientId") Long inpatientId
    );
}
