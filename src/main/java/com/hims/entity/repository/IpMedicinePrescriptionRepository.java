package com.hims.entity.repository;

import com.hims.entity.IpMedicinePrescription;
import com.hims.projection.IpMedicinePrescriptionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpMedicinePrescriptionRepository extends JpaRepository<IpMedicinePrescription,Long> {

    @Query(value = """
            SELECT
                imp.prescription_id AS prescriptionId,
                imp.inpatient_id AS inpatientId,
                imp.item_id AS itemId,
                imp.item_name AS itemName,
                imp.route_id AS routeId,
                mr.route_name AS routeName,
                imp.dose AS dose,
                imp.frequency_id AS frequencyId,
                mf.frequency_name AS frequencyName,
                imp.start_date AS startDate,
                imp.stop_date AS stopDate,
                imp.administrated_by AS administratedBy
            FROM ip_medicine_prescription imp
            LEFT JOIN mas_route mr ON mr.route_id = imp.route_id
            LEFT JOIN mas_frequency mf ON mf.frequency_id = imp.frequency_id
            WHERE imp.inpatient_id = :inpatientId
            ORDER BY imp.start_date DESC, imp.prescription_id DESC
            """, nativeQuery = true)
    List<IpMedicinePrescriptionProjection> getMedicationTreatmentByInpatientId(
            @Param("inpatientId") Long inpatientId
    );
}
