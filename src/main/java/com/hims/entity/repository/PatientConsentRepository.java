package com.hims.entity.repository;

import com.hims.entity.PatientConsent;
import com.hims.projection.PatientConsentProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PatientConsentRepository extends JpaRepository<PatientConsent, Long> {
    @Query(value = """
            SELECT pc.patient_consent_id AS patientConsentId,
                   pc.admission_id AS admissionId,
                   pc.ot_booking_id AS otBookingId,
                   ct.consent_type_id AS consentTypeId,
                   ct.consent_type_code AS consentTypeCode,
                   ct.consent_type_name AS consentTypeName,
                   s.surgery_id AS surgeryId,
                   s.surgery_name AS surgeryName,
                   pc.remarks AS remarks,
                   pc.document_name AS documentName,
                   pc.uploaded_by AS uploadedBy,
                   pc.uploaded_datetime AS uploadedDatetime
            FROM public.patient_consent pc
            JOIN public.mas_consent_type ct ON ct.consent_type_id = pc.consent_type_id
            LEFT JOIN public.mas_surgery s ON s.surgery_id = pc.surgery_id
            WHERE UPPER(pc.status) = 'Y'
              AND (CAST(:admissionId AS bigint) IS NULL OR pc.admission_id = :admissionId)
              AND (CAST(:otBookingId AS bigint) IS NULL OR pc.ot_booking_id = :otBookingId)
            ORDER BY pc.uploaded_datetime DESC, pc.patient_consent_id DESC
            """, nativeQuery = true)
    List<PatientConsentProjection> findConsentDetails(@Param("admissionId") Long admissionId,
                                                      @Param("otBookingId") Long otBookingId);
}
