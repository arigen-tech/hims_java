package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_consent", schema = "public")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatientConsent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_consent_id")
    private Long patientConsentId;

    @Column(name = "admission_id")
    private Long admissionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consent_type_id", nullable = false)
    private MasConsentType consentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_booking_id")
    private OtBooking otBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surgery_id")
    private MasSurgery surgery;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "document_path", nullable = false, columnDefinition = "text")
    private String documentPath;

    @Column(name = "uploaded_by", nullable = false, length = 200)
    private String uploadedBy;

    @Column(name = "uploaded_datetime", nullable = false)
    private LocalDateTime uploadedDatetime;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
