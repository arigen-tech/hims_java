
package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ot_pac_medical_history",
        schema = "public"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtPacMedicalHistory {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "pac_medical_history_id")
 private Long pacMedicalHistoryId;

 @JsonIgnore
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "pac_assessment_id", nullable = false)
 private OtPacAssessment pacAssessment;

 @Column(name = "disease_name", nullable = false, length = 200)
 private String diseaseName;

 @Column(name = "disease_status", length = 50)
 private String diseaseStatus;

 @Column(name = "duration", length = 100)
 private String duration;

 @Column(name = "remarks")
 private String remarks;
}
