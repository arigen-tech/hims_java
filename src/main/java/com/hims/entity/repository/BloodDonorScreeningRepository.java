package com.hims.entity.repository;

import com.hims.entity.BloodDonorScreening;
import com.hims.projection.BloodDonorDetailsProjection;
import com.hims.projection.BloodDonorPreviousScreeningProjection;
import com.hims.projection.DonorProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodDonorScreeningRepository extends JpaRepository<BloodDonorScreening,Long> {
    @Query(value = """
    SELECT
        bd.donor_id AS donorId,
        bd.donor_code AS donorCode,
        bd.first_name AS firstName,
        
        bd.last_name AS lastName,
        mg.gender_name AS gender,
        bd.date_of_birth AS dateOfBirth,
        bd.mobile_no AS mobileNo,
        bg.blood_group_id AS bloodGroupId,
        bg.blood_group_code AS bloodGroup,
        bd.donation_type_id AS donationType,
        mr.relation_name AS relation,
        bd.donor_screening_status AS donorScreeningStatus,
        bd.current_deferral_reason AS currentDeferralReason,
        bd.deferral_upto_date AS deferralUpToDate,
        bd.address_line1 AS addressLine1,
        bd.address_line2 AS addressLine2,
        bd.country_id AS country,
        mc.country_name AS countryName,
        bd.state_id AS state,
        ms.state_name AS stateName,
        bd.district_id AS district,
        md.district_name AS districtName,
        bd.city AS city,
        bd.pincode AS pinCode,
        bd.remarks AS remarks,
        bd.status AS status,
        bd.created_date AS createdDate,
        bd.created_by AS createdBy
    FROM blood_donor bd
    LEFT JOIN mas_gender mg
        ON mg.id = bd.gender_id
    LEFT JOIN mas_relation mr
        ON mr.relation_id = bd.relation
    LEFT JOIN mas_country mc
        ON mc.country_id = bd.country_id
    LEFT JOIN mas_state ms
        ON ms.state_id = bd.state_id
    LEFT JOIN mas_district md
        ON md.id = bd.district_id
      LEFT JOIN mas_blood_group bg
                   ON bg.blood_group_id = bd.blood_group_id
    WHERE bd.donor_id = :donorId
    """, nativeQuery = true)
    BloodDonorDetailsProjection getDonorBasicDetails(@Param("donorId") Long donorId);
    @Query(value = """
    SELECT
        bds.screening_id AS screeningId,
        bds.screening_date AS screeningDate,
        bds.hemoglobin AS hemoglobin,
        bds.weight_kg AS weight,
        bds.height_cm AS height,
        bds.blood_pressure AS bp,
        bds.pulse_rate AS pulse,
        bds.temperature AS temperature,
        bds.screening_result AS screeningResult,
        bds.deferral_type AS deferralType,
        bds.deferral_reason AS deferralReason,
        bds.created_by AS conductedBy
    FROM blood_donor_screening bds
    WHERE bds.donor_id = :donorId
    ORDER BY bds.screening_date DESC, bds.screening_id DESC
    """, nativeQuery = true)
    List<BloodDonorPreviousScreeningProjection> getDonorPreviousScreenings(@Param("donorId") Long donorId);
}
