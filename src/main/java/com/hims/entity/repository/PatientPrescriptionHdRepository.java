package com.hims.entity.repository;

import com.hims.entity.PatientPrescriptionHd;
import com.hims.response.PatientPrescriptionHeaderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PatientPrescriptionHdRepository extends JpaRepository<PatientPrescriptionHd, Long> {
    PatientPrescriptionHd findByPatientId(Long id);
    @Query("""
       SELECT p
       FROM PatientPrescriptionHd p
       WHERE p.visit.id = :visitId
      
       """)
    PatientPrescriptionHd findByPatientIdAndVisitId(
            @Param("visitId") Long visitId);

    PatientPrescriptionHd findByVisit_Id(Long visitId);

    Optional<PatientPrescriptionHd> findLatestByPatientId(Long patientId);

    @Query("""  
SELECT new  com.hims.response.PatientPrescriptionHeaderResponse(
    p.prescriptionHdId, 
    p.prescriptionDate, 
    CONCAT(
        COALESCE(pt.patientFn, ''), ' ',
        COALESCE(pt.patientMn, ''), ' ',
        COALESCE(pt.patientLn, '')
    ),
    pt.uhidNo, 
    pt.patientMobileNumber, 
    pt.patientAge, 
    g.genderName, 
    d.departmentName, 
    p.doctorName
    )
    FROM PatientPrescriptionHd p  
    LEFT JOIN p.department d 
    LEFT JOIN p.patient pt 
    LEFT JOIN pt.patientGender g
    WHERE p.hospitalId = :hospitalId
     AND p.status = :pendingStatus
AND (:departmentId IS NULL OR d.id = :departmentId)
AND (:patientName IS NULL OR CONCAT(
        COALESCE(pt.patientFn, ''), ' ',
        COALESCE(pt.patientMn, ''), ' ',
        COALESCE(pt.patientLn, '')
    ) LIKE %:patientName%)
AND (:patientMobileNo IS NULL OR pt.patientMobileNumber = :patientMobileNo)
""")
    Page<PatientPrescriptionHeaderResponse> findPendingPrescriptionsHeaders(
            Long hospitalId,
            Long departmentId,
            String patientName,
            String patientMobileNo,
            String pendingStatus,
            Pageable pageable
    );
}
