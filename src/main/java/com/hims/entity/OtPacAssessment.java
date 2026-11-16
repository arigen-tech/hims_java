
package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "ot_pac_assessment",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pac_booking_assessment",
                        columnNames = {"ot_booking_id", "assessment_no"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtPacAssessment {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "pac_assessment_id")
 private Long pacAssessmentId;

 @JsonIgnore
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "ot_booking_id", nullable = false)
 private OtBooking otBooking;

 @Column(name = "assessment_no", nullable = false)
 private Integer assessmentNo;

 @Column(name = "anaesthetist_id")
 private Long anaesthetistId;

 @Column(name = "assessment_date", nullable = false)
 private LocalDateTime assessmentDate;

 @Column(name = "assessment_status", nullable = false, length = 20)
 private String assessmentStatus;

 @Column(name = "finalized_date")
 private LocalDateTime finalizedDate;

 @Column(name = "current_medications")
 private String currentMedications;

 @Column(name = "allergy_status", length = 20)
 private String allergyStatus;

 @Column(name = "allergy_details")
 private String allergyDetails;

 @Column(name = "previous_anaesthesia", length = 1)
 private String previousAnaesthesia;

 @Column(name = "previous_anaesthesia_details")
 private String previousAnaesthesiaDetails;

 @Column(name = "weight_kg", precision = 6, scale = 2)
 private BigDecimal weightKg;

 @Column(name = "bp_systolic")
 private Short bpSystolic;

 @Column(name = "bp_diastolic")
 private Short bpDiastolic;

 @Column(name = "pulse")
 private Short pulse;

 @Column(name = "respiratory_rate")
 private Short respiratoryRate;

 @Column(name = "spo2", precision = 5, scale = 2)
 private BigDecimal spo2;

 @Column(name = "temperature", precision = 4, scale = 1)
 private BigDecimal temperature;

 @Column(name = "pallor", length = 1)
 private String pallor;

 @Column(name = "icterus", length = 1)
 private String icterus;

 @Column(name = "cyanosis", length = 1)
 private String cyanosis;

 @Column(name = "koilonychia", length = 1)
 private String koilonychia;

 @Column(name = "oedema", length = 1)
 private String oedema;

 @Column(name = "lymphadenopathy", length = 1)
 private String lymphadenopathy;

 @Column(name = "mallampati_class", length = 10)
 private String mallampatiClass;

 @Column(name = "thyromental_distance_cm", precision = 5, scale = 2)
 private BigDecimal thyromentalDistanceCm;

 @Column(name = "mouth_opening_cm", precision = 5, scale = 2)
 private BigDecimal mouthOpeningCm;

 @Column(name = "tmj_findings")
 private String tmjFindings;

 @Column(name = "teeth_dentures")
 private String teethDentures;

 @Column(name = "neck_movement")
 private String neckMovement;

 @Column(name = "iv_access")
 private String ivAccess;

 @Column(name = "spine_findings")
 private String spineFindings;

 @Column(name = "cvs_findings")
 private String cvsFindings;

 @Column(name = "rs_findings")
 private String rsFindings;

 @Column(name = "cns_findings")
 private String cnsFindings;

 @Column(name = "abdomen_findings")
 private String abdomenFindings;

 @Column(name = "investigation_review_status", length = 30)
 private String investigationReviewStatus;

 @Column(name = "investigation_findings")
 private String investigationFindings;

 @Column(name = "asa_grade_id")
 private Long asaGradeId;

 @Column(name = "asa_emergency", nullable = false, length = 1)
 private String asaEmergency;

 @Column(name = "anaesthesia_type_id")
 private Long anaesthesiaTypeId;

 @Column(name = "specialist_referral_required", length = 1)
 private String specialistReferralRequired;

 @Column(name = "specialist_department_id")
 private Long specialistDepartmentId;

 @Column(name = "specialist_referral_reason")
 private String specialistReferralReason;

 @Column(name = "clearance_status", nullable = false, length = 20)
 private String clearanceStatus;

 @Column(name = "pac_remarks")
 private String pacRemarks;

 @Column(name = "status", nullable = false, length = 1)
 private String status;

 @Column(name = "last_chg_by", length = 200)
 private String lastChgBy;

 @Column(name = "last_chg_date", nullable = false)
 private LocalDateTime lastChgDate;
}
