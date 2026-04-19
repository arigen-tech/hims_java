package com.hims.entity.repository;

import com.hims.entity.BloodDonor;
import com.hims.projection.BloodDonorCollectionDetailsProjection;
import com.hims.projection.BloodDonorCollectionProjection;
import com.hims.projection.DonorProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodDonorRepository extends JpaRepository<BloodDonor,Long> {

    @Query(value = """
    SELECT donor_code FROM blood_donor 
    WHERE donor_code LIKE CONCAT(:prefix, '%') 
    ORDER BY donor_code DESC LIMIT 1 
    """, nativeQuery = true)
    String findLastDonorCodeByPrefix(@Param("prefix") String prefix);

    @Query(value = """
        SELECT CASE 
                 WHEN COUNT(*) > 0 THEN true 
                 ELSE false 
               END
        FROM blood_donor bd
        WHERE bd.mobile_no = :mobileNo
          AND bd.first_name = :firstName
          AND bd.date_of_birth = :dateOfBirth
          AND bd.relation = :relationId
          AND bd.blood_group_id = :bloodGroupId
        """, nativeQuery = true)
    boolean existsDonorByDetails(
            @Param("mobileNo") String mobileNo,
            @Param("firstName") String firstName,
            @Param("dateOfBirth") LocalDate dateOfBirth,
            @Param("relationId") Long relationId,
            @Param("bloodGroupId") Long bloodGroupId
    );

    @Query(value = """
    SELECT
        bd.donor_id AS donorId,
        NULL AS screeningId,
        bd.donor_code AS donorCode,
        CONCAT_WS(' ', bd.first_name, bd.last_name) AS name,
        mg.gender_name AS gender,
        bd.mobile_no AS mobileNo,
        mbg.blood_group_name AS bloodGroup,
        CAST(bd.created_date AS DATE) AS registrationDate,
        bd.donor_screening_status AS screeningResult
    FROM blood_donor bd
    LEFT JOIN mas_gender mg ON mg.id = bd.gender_id
    LEFT JOIN mas_blood_group mbg ON mbg.blood_group_id = bd.blood_group_id
    WHERE
        (:donorName IS NULL OR TRIM(:donorName) = '' 
         OR LOWER(CONCAT_WS(' ', bd.first_name, bd.last_name)) LIKE LOWER(CONCAT('%', :donorName, '%')))
      AND
        (:mobileNo IS NULL OR TRIM(:mobileNo) = '' 
         OR bd.mobile_no LIKE CONCAT('%', :mobileNo, '%'))
      AND bd.hospital_id=:hospitalId
    ORDER BY bd.donor_id DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM blood_donor bd
    LEFT JOIN mas_gender mg ON mg.id = bd.gender_id
    LEFT JOIN mas_blood_group mbg ON mbg.blood_group_id = bd.blood_group_id
    WHERE
        (:donorName IS NULL OR TRIM(:donorName) = '' 
         OR LOWER(CONCAT_WS(' ', bd.first_name, bd.last_name)) LIKE LOWER(CONCAT('%', :donorName, '%')))
      AND
        (:mobileNo IS NULL OR TRIM(:mobileNo) = '' 
         OR bd.mobile_no LIKE CONCAT('%', :mobileNo, '%'))
    """,
            nativeQuery = true)
    Page<DonorProjection> getAllDonor(@Param("hospitalId") Long hospitalId,
            Pageable pageable,
            @Param("donorName") String donorName,
            @Param("mobileNo") String mobileNo);

@Query(value = """
        SELECT
            bd.donor_id AS donorId,
            bd.donor_code AS donorCode,
            bd.first_name AS firstName,
            bd.last_name AS lastName,
            bg.blood_group_id AS bloodGroupId,
            bg.blood_group_name AS bloodGroup,
            bs.screening_date AS lastScreening,
            bs.hemoglobin AS hb,
            bs.weight_kg AS weight
        FROM blood_donor bd
        LEFT JOIN mas_blood_group bg
               ON bg.blood_group_id = bd.blood_group_id
        LEFT JOIN (
            SELECT
                bds.screening_id,
                bds.donor_id,
                bds.screening_date,
                bds.hemoglobin,
                bds.weight_kg,
                bds.screening_result,
                ROW_NUMBER() OVER (
                    PARTITION BY bds.donor_id
                    ORDER BY bds.screening_date DESC, bds.screening_id DESC
                ) AS rn
            FROM blood_donor_screening bds
        ) bs
               ON bs.donor_id = bd.donor_id
              AND bs.rn = 1
        LEFT JOIN blood_donation_hdr d
               ON d.screening_id = bs.screening_id
        WHERE LOWER(bd.donor_screening_status) = LOWER(:donorScreeningStatus)
          AND LOWER(bs.screening_result) = LOWER(:donorScreeningStatus)
          AND d.donation_id IS NULL AND bd.hospital_id=:hospitalId
        """, nativeQuery = true)
    List<BloodDonorCollectionProjection> findPendingBloodCollection(@Param("donorScreeningStatus") String donorScreeningStatusPass,
                                                                    @Param("hospitalId") Long hospitalId);

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
            bg.blood_group_name AS bloodGroup,
            bd.donor_screening_status AS donorScreeningStatus,
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
            bs.screening_id AS screeningId,
            bs.screening_date AS screeningDate,
            bs.hemoglobin AS hemoglobin,
            bs.weight_kg AS weight,
            bs.height_cm AS height,
            bs.blood_pressure AS bp,
            bs.pulse_rate AS pulse,
            bs.temperature AS temperature
        FROM blood_donor bd
        LEFT JOIN mas_gender mg
               ON mg.id = bd.gender_id
        LEFT JOIN mas_blood_group bg
               ON bg.blood_group_id = bd.blood_group_id
        LEFT JOIN mas_country mc
               ON mc.country_id = bd.country_id
        LEFT JOIN mas_state ms
               ON ms.state_id = bd.state_id
        LEFT JOIN mas_district md
               ON md.id = bd.district_id
        LEFT JOIN (
            SELECT
                bds.screening_id,
                bds.donor_id,
                bds.screening_date,
                bds.hemoglobin,
                bds.weight_kg,
                bds.height_cm,
                bds.blood_pressure,
                bds.pulse_rate,
                bds.temperature,
                ROW_NUMBER() OVER (
                    PARTITION BY bds.donor_id
                    ORDER BY bds.screening_date DESC, bds.screening_id DESC
                ) AS rn
            FROM blood_donor_screening bds
        ) bs
               ON bs.donor_id = bd.donor_id
              AND bs.rn = 1
        WHERE bd.donor_id = :donorId AND bd.hospital_id=:hospitalId
        """, nativeQuery = true)
    Optional<BloodDonorCollectionDetailsProjection> findPendingBloodCollectionDetails(@Param("donorId") Long donorId,
                                                                                      @Param("hospitalId") Long hospitalId);
}

