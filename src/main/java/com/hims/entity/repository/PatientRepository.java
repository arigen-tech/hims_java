package com.hims.entity.repository;


import com.hims.entity.MasGender;
import com.hims.entity.MasRelation;
import com.hims.entity.Patient;
//import com.hims.projection.CancellationReportProjection;
import com.hims.projection.PatientProjection;
import com.hims.projection.PatientProjectionFollowUpDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByPatientMobileNumberAndPatientRelation(String pMobileNumber, MasRelation pRelation);

    @Query("SELECT p FROM Patient p WHERE p.patientFn = :firstName AND p.patientLn = :lastName AND p.patientGender = :gender " +
            "AND (p.patientDob = :dob OR p.patientAge = :age) AND p.patientMobileNumber = :mobileNumber AND p.patientRelation = :relation")
    Optional<Patient> findByUniqueCombination(String firstName, String lastName, MasGender gender,
                                              LocalDate dob, String age, String mobileNumber, MasRelation relation);

    @Query(value = """
            SELECT 
                CONCAT(
                    COALESCE(CAST(p.p_fn AS TEXT), ''), ' ',
                    COALESCE(CAST(p.p_mn AS TEXT), ''), ' ',
                    COALESCE(CAST(p.p_ln AS TEXT), '')
                ) AS fullName,
            
                p.patient_id AS id,
            
                CONCAT(
                    COALESCE(CAST(p.p_address1 AS TEXT), ''), ' ',
                    COALESCE(CAST(p.p_address2 AS TEXT), ''), ' ',
                    COALESCE(CAST(p.p_city AS TEXT), ''), ' ',
                    COALESCE(CAST(p.p_pincode AS TEXT), '')
                ) AS address,
            
                p.p_mobile_number AS patientMobileNumber,
                p.uhid_no AS uhidNo,
                p.p_age AS patientAge,
                g.gender_name AS gender,
                p.p_email_id AS patientEmailId,
                m.relation_name AS relation
            
            FROM patient p
            LEFT JOIN mas_gender g ON g.id = p.p_gender_id
            LEFT JOIN mas_relation m ON m.relation_id = p.p_relation_id
            
            WHERE (:mobileNo IS NULL OR CAST(p.p_mobile_number AS TEXT) = :mobileNo)
            AND (:patientName IS NULL OR 
                LOWER(CONCAT(
                    COALESCE(CAST(p.p_fn AS TEXT), ''), ' ',
                    COALESCE(CAST(p.p_mn AS TEXT), ''), ' ',
                    COALESCE(CAST(p.p_ln AS TEXT), '')
                )) LIKE LOWER(CONCAT('%', :patientName, '%'))
            )
            """,
            nativeQuery = true,

            countQuery = """
                    SELECT COUNT(*) 
                    FROM patient p
                    WHERE (:mobileNo IS NULL OR CAST(p.p_mobile_number AS TEXT) = :mobileNo)
                    AND (:patientName IS NULL OR 
                        LOWER(CONCAT(
                            COALESCE(CAST(p.p_fn AS TEXT), ''), ' ',
                            COALESCE(CAST(p.p_mn AS TEXT), ''), ' ',
                            COALESCE(CAST(p.p_ln AS TEXT), '')
                        )) LIKE LOWER(CONCAT('%', :patientName, '%'))
                    )
                    """)
    Page<PatientProjection> searchPatients(
            @Param("mobileNo") String mobileNo,
            @Param("patientName") String patientName,
            Pageable pageable);


    @Query(value = """
            SELECT DISTINCT p.* FROM patient p 
            LEFT JOIN appointment a ON p.patient_id = a.patient_id
            WHERE (:mobileNo IS NULL OR CAST(p.p_mobile_number AS TEXT) ILIKE CONCAT('%', :mobileNo, '%'))
            AND (:patientName IS NULL OR 
                 CAST(p.p_fn AS TEXT) ILIKE CONCAT('%', :patientName, '%') OR 
                 CAST(p.p_mn AS TEXT) ILIKE CONCAT('%', :patientName, '%') OR 
                 CAST(p.p_ln AS TEXT) ILIKE CONCAT('%', :patientName, '%'))
            AND (:uhidNo IS NULL OR CAST(p.uhid_no AS TEXT) ILIKE CONCAT('%', :uhidNo, '%'))
            AND (:appointmentDate IS NULL OR DATE(a.appointment_date) = :appointmentDate)
            """, nativeQuery = true)
    List<Patient> searchPatients(@Param("mobileNo") String mobileNo,
                                 @Param("patientName") String patientName,
                                 @Param("uhidNo") String uhidNo,
                                 @Param("appointmentDate") LocalDate appointmentDate);

    @Query("""
            SELECT p FROM Patient p
            LEFT JOIN FETCH p.patientGender
            LEFT JOIN FETCH p.patientRelation
            WHERE (:mobileNo IS NULL OR p.patientMobileNumber LIKE %:mobileNo%)
            AND (:patientName IS NULL OR 
                 p.patientFn LIKE %:patientName% OR
                 p.patientMn LIKE %:patientName% OR
                 p.patientLn LIKE %:patientName%)
            AND (:uhidNo IS NULL OR p.uhidNo LIKE %:uhidNo%)
            """)
    List<Patient> searchPatients(@Param("mobileNo") String mobileNo,
                                 @Param("patientName") String patientName,
                                 @Param("uhidNo") String uhidNo);


    boolean existsByPatientFnAndPatientDobAndPatientGenderIdAndPatientMobileNumberAndPatientRelationId(String trim, LocalDate parse, Long gender, String trim1, Long relation);


    @Query(value = """
    SELECT 
        p.patient_id AS patientId,
        p.p_fn AS firstName,
        p.p_mn AS middleName,
        p.p_ln AS lastName,
        p.p_mobile_number AS mobileNo,
        p.p_email_id AS email,
        p.p_dob AS dob,
        g.id AS genderId,
        g.gender_name AS genderName,
        r.relation_id AS relationId,
        r.relation_name AS relationName,

        p.p_address1 AS address1,
        p.p_address2 AS address2,
        p.p_city AS city,
        p.p_pincode AS pinCode,
        c.country_id AS countryId,
        c.country_name AS countryName,
        s.state_id AS stateId,
        s.state_name AS stateName,
        d.id AS districtId,
        d.district_name AS districtName,

        p.nok_fn AS nokFirstName,
        p.nok_ln AS nokLastName,
        p.nok_email AS nokEmail,
        p.nok_mobile_number AS nokMobile,
        p.nok_address1 AS nokAddress1,
        p.nok_address2 AS nokAddress2,
        p.nok_city AS nokCity,
        p.nok_pincode AS nokPinCode,
        nc.country_id AS nokCountryId,
        nc.country_name AS nokCountryName,
        ns.state_id AS nokStateId,
        ns.state_name AS nokStateName,
        nd.id AS nokDistrictId,
        nd.district_name AS nokDistrictName,

        p.emer_fn AS emerFirstName,
        p.emer_ln AS emerLastName,
        p.emer_mobile AS emerMobile,

        p.patient_image AS photoUrl

    FROM patient p
    LEFT JOIN mas_gender g ON p.p_gender_id = g.id
    LEFT JOIN mas_relation r ON p.p_relation_id = r.relation_id
    LEFT JOIN mas_country c ON p.p_country_id = c.country_id
    LEFT JOIN mas_state s ON p.p_state_id = s.state_id
    LEFT JOIN mas_district d ON p.p_district_id = d.id

    LEFT JOIN mas_country nc ON p.nok_country_id = nc.country_id
    LEFT JOIN mas_state ns ON p.nok_state_id = ns.state_id
    LEFT JOIN mas_district nd ON p.nok_district_id = nd.id

    WHERE p.patient_id = :patientId
""", nativeQuery = true)
    PatientProjectionFollowUpDetails findPatientDetails(Long patientId);
}
